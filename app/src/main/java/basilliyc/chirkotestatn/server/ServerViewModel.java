package basilliyc.chirkotestatn.server;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import basilliyc.chirkotestatn.Constants;
import basilliyc.chirkotestatn.base.BaseWorkViewModel;
import basilliyc.chirkotestatn.entity.LoadingMediaInfo;
import basilliyc.chirkotestatn.entity.SendMediaInfo;
import basilliyc.chirkotestatn.utils.Error;
import basilliyc.chirkotestatn.utils.Utils;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ServerViewModel extends BaseWorkViewModel {

    private Disposable disposableServerSocket;
    public MutableLiveData<File> selectedDir = new MutableLiveData<>();
    public MutableLiveData<ServerStatus> serverStatus = new MutableLiveData<>(ServerStatus.DIR_NOT_SELECTED);
    public MutableLiveData<LoadingMediaInfo> loadingProgress = new MutableLiveData<>(null);

    private ServerSocket serverSocket;
    private Socket client;

    private void startServerSocket() {
        stopServerSocket();

        File dir = selectedDir.getValue();
        if (dir == null) {
            onError(new Throwable(Error.DIR_NOT_SELECTED));
            serverStatus.postValue(ServerStatus.DIR_NOT_SELECTED);
            return;
        }

        dir.mkdirs();

        if (!dir.exists()) {
            onError(new Throwable(Error.DIR_NOT_EXISTS));
            selectedDir.postValue(null);
            serverStatus.postValue(ServerStatus.DIR_NOT_SELECTED);
            return;
        }

        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    processServer();
                    emitter.onComplete();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();

                    if (throwable instanceof SocketException && "Socket closed".equals(throwable.getMessage())) {
                        stopServerSocket();
                    } else {
                        emitter.onError(throwable);
                    }

                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposableServerSocket = d;
                        compositeDisposable.add(d);
                        serverStatus.postValue(ServerStatus.WAIT_FOR_DATA);
                    }

                    @Override
                    public void onComplete() {
                        loadingProgress.postValue(null);
                        serverStatus.postValue(ServerStatus.STOPPED);
                        startServerSocket();
                    }

                    @Override
                    public void onError(Throwable e) {
                        stopServerSocket();
                        ServerViewModel.this.onError(e);
                    }
                });


    }

    void stopServerSocket() {
        loadingProgress.postValue(null);

        if (disposableServerSocket != null && !disposableServerSocket.isDisposed()) {
            disposableServerSocket.dispose();
        }

        disposableServerSocket = null;
        serverStatus.postValue(ServerStatus.STOPPED);

        try {
            if (client != null) {
                client.close();
            }

            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processServer() throws Throwable {
        serverSocket = new ServerSocket(Constants.SOCKET_PORT);
        client = serverSocket.accept();
        serverStatus.postValue(ServerStatus.LOADING);
        InputStream inputStream = client.getInputStream();

        //read media info

        StringBuilder builder = new StringBuilder();
        int r;
        while ((r = inputStream.read()) != -1) {
            char ch = (char) r;
            builder.append(ch);
            if (ch == '}') {
                break;
            }
        }

        String next = builder.toString();
        Gson gson = new Gson();
        SendMediaInfo sendMediaInfo = gson.fromJson(next, SendMediaInfo.class);
        Utils.log(sendMediaInfo);

        File dir = selectedDir.getValue();
        dir.mkdirs();
        File file = new File(dir, sendMediaInfo.getFileName());
        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file);


        LoadingMediaInfo progressValue = new LoadingMediaInfo();
        progressValue.setFileName(sendMediaInfo.getFileName());
        progressValue.setFileLength(sendMediaInfo.getFileLength());
        progressValue.setLoadingLength(0L);
        loadingProgress.postValue(progressValue);

        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, len);

            progressValue.setLoadingLength(progressValue.getLoadingLength() + len);
            loadingProgress.postValue(progressValue);
        }



        fileOutputStream.close();
        inputStream.close();
        client.close();
        client = null;
        serverSocket.close();
        serverSocket = null;
    }

    public void onDirSelected(String path, boolean startServer) {
        if (disposableServerSocket != null) {
            stopServerSocket();
        }

        File dir = new File(path);
        dir.mkdirs();

        if (dir.exists()) {
            selectedDir.postValue(dir);
            preferences.setLastDir(path);
            serverStatus.postValue(ServerStatus.STOPPED);

            if (startServer)
                startServerSocket();
        }
    }


    public void toggleServerWork() {
        if (disposableServerSocket != null) {
            stopServerSocket();
        } else {
            startServerSocket();
        }
    }
}

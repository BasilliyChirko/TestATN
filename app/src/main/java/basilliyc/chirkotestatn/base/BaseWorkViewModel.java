package basilliyc.chirkotestatn.base;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import basilliyc.chirkotestatn.Constants;
import basilliyc.chirkotestatn.Preferences;
import basilliyc.chirkotestatn.client.ClientViewModel;
import basilliyc.chirkotestatn.entity.LoadingMediaInfo;
import basilliyc.chirkotestatn.entity.SendMediaInfo;
import basilliyc.chirkotestatn.entity.StoredMediaInfo;
import basilliyc.chirkotestatn.server.SocketStatus;
import basilliyc.chirkotestatn.utils.Error;
import basilliyc.chirkotestatn.utils.Utils;
import basilliyc.chirkotestatn.utils.action.ActionCallLiveData;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BaseWorkViewModel extends ViewModel {

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    public Preferences preferences;

    public ActionCallLiveData<Throwable> error = new ActionCallLiveData<>();

    public MutableLiveData<SocketStatus> socketStatus = new MutableLiveData<>(SocketStatus.DISCONNECTED);
    public MutableLiveData<LoadingMediaInfo> loadingProgress = new MutableLiveData<>(null);

    public MutableLiveData<File> selectedDir = new MutableLiveData<>();
    public MutableLiveData<ArrayList<File>> selectedMedia = new MutableLiveData<>(new ArrayList<File>());

    public MutableLiveData<ArrayList<StoredMediaInfo>> storedMedia = new MutableLiveData<>(new ArrayList<StoredMediaInfo>());
    public ActionCallLiveData<StoredMediaInfo> newStoredMedia = new ActionCallLiveData<>();


    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected Socket socket;
    protected Disposable disposableSocket;
    protected Disposable disposableLoading;

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public void onError(Throwable throwable) {
        error.activate(throwable);
    }

    public void onDirSelected(String path) {
        File dir = new File(path);
        dir.mkdirs();

        if (dir.exists()) {
            selectedDir.postValue(dir);
            preferences.setLastDir(path);
            socketStatus.postValue(SocketStatus.DISCONNECTED);
        }
    }

    public void onMediaSelected(String path) {
        Utils.log(path);
        File file = new File(path);
        if (file.exists()) {
            ArrayList<File> value = selectedMedia.getValue();
            if (value == null) {
                value = new ArrayList<>();
            }
            value.add(file);
            selectedMedia.postValue(value);
        }
    }

    protected void onDeviceConnected(Socket socket) throws Throwable {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        socketStatus.postValue(SocketStatus.CONNECTED);

        receiveMedia();
    }

    public void disconnectSocket() {
        loadingProgress.postValue(null);

        if (disposableSocket != null && !disposableSocket.isDisposed()) {
            disposableSocket.dispose();
        }
        disposableSocket = null;
        socketStatus.postValue(SocketStatus.DISCONNECTED);
    }

    protected void sendMedia(final Context context) {
        final ArrayList<File> files = selectedMedia.getValue();

        if (files == null) {
            onError(new Throwable(Error.FILE_NOT_SELECTED));
            return;
        }

        for (File file : files) {
            if (!file.exists()) {
                onError(new Throwable(Error.FILE_NOT_EXISTS));
                return;
            }
        }

        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    for (File file : files) {
                        sendSingleFile(file, context);
                    }
                    selectedMedia.postValue(new ArrayList<File>());
                    emitter.onComplete();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    if (throwable instanceof SocketException && "Socket closed".equals(throwable.getMessage())) {
                        disconnectSocket();
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
                        disposableLoading = d;
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        disposableLoading = null;
                    }

                    @Override
                    public void onError(Throwable e) {
                        disconnectSocket();
                        BaseWorkViewModel.this.onError(e);
                    }
                })
        ;
    }

    private void sendSingleFile(File file, Context context) throws Throwable {
        socketStatus.postValue(SocketStatus.UPLOADING);

        SendMediaInfo sendMediaInfo = new SendMediaInfo();
        sendMediaInfo.setFileName(file.getName());
        sendMediaInfo.setFileLength(file.length());

        Gson gson = new Gson();
        String s = gson.toJson(sendMediaInfo);
        outputStream.write(s.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        LoadingMediaInfo progressValue = new LoadingMediaInfo();
        progressValue.setFileName(sendMediaInfo.getFileName());
        progressValue.setFileLength(sendMediaInfo.getFileLength());
        progressValue.setLoadingLength(0L);
        loadingProgress.postValue(progressValue);

        byte buf[] = new byte[1024];
        int len;
        InputStream streamFromFile = context.getContentResolver().openInputStream(Uri.fromFile(file));
        if (streamFromFile == null) {
            throw new Throwable(Error.FILE_ERROR_OPEN_STREAM);
        }
        while ((len = streamFromFile.read(buf)) != -1) {
            outputStream.write(buf, 0, len);

            progressValue.setLoadingLength(progressValue.getLoadingLength() + len);
            loadingProgress.postValue(progressValue);
        }
        streamFromFile.close();

        addToStoredMedia(progressValue, file.getPath(), false);

        loadingProgress.postValue(null);

        socketStatus.postValue(SocketStatus.CONNECTED);
    }

    protected void receiveMedia() {
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    while (true) {
                        receiveSingleFile();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    if (throwable instanceof SocketException && "Socket closed".equals(throwable.getMessage())) {
                        disconnectSocket();
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
                        disposableLoading = d;
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete will never execute");
                    }

                    @Override
                    public void onError(Throwable e) {
                        disconnectSocket();
                        BaseWorkViewModel.this.onError(e);
                    }
                })
        ;
    }

    protected void receiveSingleFile() throws Exception {
        StringBuilder builder = new StringBuilder();
        int r;
        while ((r = inputStream.read()) != -1) {
            char ch = (char) r;
            builder.append(ch);
            if (ch == '}') {
                break;
            }
        }
        socketStatus.postValue(SocketStatus.DOWNLOADING);

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

        byte[] buffer;
        byte[] defaultBuffer = new byte[1024];
        int len;
        buffer = defaultBuffer;
        long downloaded = 0;
        while (true) {
            int l = (int) (sendMediaInfo.getFileLength() - downloaded);
            if (l < 1024) {
                buffer = new byte[l];
                if (l == 0) {
                    break;
                }
            }
            len = inputStream.read(buffer);
            downloaded += len;
            if (len == -1) {
                break;
            }

            fileOutputStream.write(buffer, 0, len);

            progressValue.setLoadingLength(progressValue.getLoadingLength() + len);
            loadingProgress.postValue(progressValue);
        }

        fileOutputStream.close();

        addToStoredMedia(progressValue, file.getPath(), true);
        loadingProgress.postValue(null);
        socketStatus.postValue(SocketStatus.CONNECTED);

    }

    private void addToStoredMedia(LoadingMediaInfo loadingMediaInfo, String path, boolean isReceived) {
        StoredMediaInfo storedMediaInfo = new StoredMediaInfo();
        storedMediaInfo.setFileName(loadingMediaInfo.getFileName());
        storedMediaInfo.setFileLength(loadingMediaInfo.getFileLength());
        storedMediaInfo.setFilePath(path);
        storedMediaInfo.setTime(System.currentTimeMillis());
        storedMediaInfo.setReceived(isReceived);

        ArrayList<StoredMediaInfo> storedMediaValue = storedMedia.getValue();
        if (storedMediaValue == null) {
            storedMediaValue = new ArrayList<>();
        }
        storedMediaValue.add(0, storedMediaInfo);
        newStoredMedia.activate(storedMediaInfo);
    }

    public void clearMedia() {
        selectedMedia.postValue(new ArrayList<File>());
    }
}

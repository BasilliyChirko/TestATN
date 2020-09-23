package basilliyc.chirkotestatn.client;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import basilliyc.chirkotestatn.Constants;
import basilliyc.chirkotestatn.base.BaseWorkViewModel;
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

public class ClientViewModel extends BaseWorkViewModel {

    public MutableLiveData<File> selectedMedia = new MutableLiveData<>();

    public Disposable disposableSend;

    public String getLastIp() {
        return preferences.getLastSendIp();
    }

    public void sendMedia(final String host, final Context context) {
        preferences.setLastSendIp(host);

        final File value = selectedMedia.getValue();

        if (value == null) {
            onError(new Throwable(Error.FILE_NOT_SELECTED));
            return;
        }

        if (!value.exists()) {
            onError(new Throwable(Error.FILE_NOT_EXISTS));
            return;
        }

        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    processMedia(host, value, context);
                    emitter.onComplete();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    emitter.onError(throwable);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposableSend = d;
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        //TODO
                        Utils.log("sef");
                    }

                    @Override
                    public void onError(Throwable e) {
                        ClientViewModel.this.onError(e);
                    }
                })
        ;
    }

    private void processMedia(String host, File file, Context context) throws Throwable {
        Socket socket = new Socket(host, Constants.SOCKET_PORT);

        OutputStream outputStream = socket.getOutputStream();

        SendMediaInfo sendMediaInfo = new SendMediaInfo();
        sendMediaInfo.setFileName(file.getName());
        sendMediaInfo.setFileLength(file.length());

        Gson gson = new Gson();
        String s = gson.toJson(sendMediaInfo);
        outputStream.write(s.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        byte buf[] = new byte[1024];
        int len;

        InputStream streamFromFile = context.getContentResolver().openInputStream(Uri.fromFile(file));
        if (streamFromFile == null) {
            throw new Throwable(Error.FILE_ERROR_OPEN_STREAM);
        }
        while ((len = streamFromFile.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
        }
        outputStream.close();
        streamFromFile.close();

        outputStream.close();
    }


    public void stopSendMedia() {
        if (disposableSend != null && !disposableSend.isDisposed()) {
            disposableSend.dispose();
        }
        disposableSend = null;
    }

    public void onMediaSelected(String path) {
        Utils.log(path);
        File file = new File(path);
        if (file.exists()) {
            selectedMedia.postValue(file);
        }

    }

}

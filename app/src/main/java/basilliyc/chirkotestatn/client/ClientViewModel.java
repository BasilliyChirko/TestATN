package basilliyc.chirkotestatn.client;

import java.io.File;
import java.net.Socket;
import java.net.SocketException;

import basilliyc.chirkotestatn.Constants;
import basilliyc.chirkotestatn.base.BaseWorkViewModel;
import basilliyc.chirkotestatn.server.SocketStatus;
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

    public String getLastIp() {
        return preferences.getLastSendIp();
    }

    public void connectToServer(final String host) {
        disconnectSocket();
        preferences.setLastSendIp(host);

        File dir = selectedDir.getValue();
        if (dir == null) {
            onError(new Throwable(Error.DIR_NOT_SELECTED));
            socketStatus.postValue(SocketStatus.DISCONNECTED);
            return;
        }

        dir.mkdirs();

        if (!dir.exists()) {
            onError(new Throwable(Error.DIR_NOT_EXISTS));
            selectedDir.postValue(null);
            socketStatus.postValue(SocketStatus.DISCONNECTED);
            return;
        }

        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    Socket socket = new Socket(host, Constants.SOCKET_PORT);
                    onDeviceConnected(socket);
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
                        disposableSocket = d;
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete will never execute");
                    }

                    @Override
                    public void onError(Throwable e) {
                        disconnectSocket();
                        ClientViewModel.this.onError(e);
                    }
                })
        ;

    }

    public void toggleConnection(String host) {
        if (disposableSocket != null) {
            disconnectSocket();
        } else {
            connectToServer(host);
        }
    }
}

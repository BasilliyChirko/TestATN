package basilliyc.chirkotestatn.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import basilliyc.chirkotestatn.Constants;
import basilliyc.chirkotestatn.base.BaseWorkViewModel;
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

    private ServerSocket serverSocket;

    private void startServerSocket() {
        disconnectSocket();

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
                    serverSocket = new ServerSocket(Constants.SOCKET_PORT);
                    Socket client = serverSocket.accept();

                    onDeviceConnected(client);
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
                        socketStatus.postValue(SocketStatus.WAIT_FOR_CLIENT);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete will never execute");
                    }

                    @Override
                    public void onError(Throwable e) {
                        disconnectSocket();
                        ServerViewModel.this.onError(e);
                    }
                });


    }

    public void toggleServerSocket() {
        if (disposableSocket != null) {
            disconnectSocket();
        } else {
            startServerSocket();
        }
    }

    @Override
    public void disconnectSocket() {
        super.disconnectSocket();

        if (disposableSocket != null && !disposableSocket.isDisposed()) {
            disposableSocket.dispose();
        }

        disposableSocket = null;
        socketStatus.postValue(SocketStatus.DISCONNECTED);

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

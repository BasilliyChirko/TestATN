package basilliyc.chirkotestatn.server;

import com.google.gson.Gson;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import basilliyc.chirkotestatn.Constants;
import basilliyc.chirkotestatn.base.BaseWorkViewModel;
import basilliyc.chirkotestatn.entity.SendMediaInfo;
import basilliyc.chirkotestatn.utils.Utils;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ServerViewModel extends BaseWorkViewModel {

    private Disposable disposableServerSocket;

    void startServerSocket() {
        stopServerSocket();

        disposableServerSocket = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                ServerSocket serverSocket = new ServerSocket(Constants.SOCKET_PORT);
                Utils.log("server 2");
                Socket client = serverSocket.accept();
                Utils.log(client);

                InputStream inputStream = client.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                String next = scanner.next();
                Gson gson = new Gson();
                SendMediaInfo sendMediaInfo = gson.fromJson(next, SendMediaInfo.class);
                Utils.log(sendMediaInfo);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action() {
                            @Override
                            public void run() {
                                Utils.log("Server Socket end ");
                                startServerSocket();
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                Utils.log("Throwable " + throwable.toString());
                            }
                        }
                );

        compositeDisposable.add(disposableServerSocket);

    }

    void stopServerSocket() {
        if (disposableServerSocket != null && !disposableServerSocket.isDisposed()) {
            disposableServerSocket.dispose();
        }

        disposableServerSocket = null;
    }
}

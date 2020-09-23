package basilliyc.chirkotestatn.base;

import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.net.InetAddress;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BaseWorkViewModel extends ViewModel {

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

    protected WifiP2pManager wifiManager;
    protected WifiP2pManager.Channel wifiChannel;


    public WifiP2pInfo connectedDeviceInfo;
    public MutableLiveData<String> connectedDeviceAddress = new MutableLiveData<>("");


    public void onWifiStateChanged() {

    }

    public void onWifiPeersChanged() {

    }

    public void onWifiConnectionChanged() {
        wifiManager.requestConnectionInfo(wifiChannel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {
                onSetConnectedDeviceInfo(wifiP2pInfo);
                compositeDisposable.add(Single.create(new SingleOnSubscribe<String>() {
                    @Override
                    public void subscribe(SingleEmitter<String> emitter) {
                        InetAddress address = wifiP2pInfo.groupOwnerAddress;
                        String canonicalHostName = address.getCanonicalHostName();
                        emitter.onSuccess(canonicalHostName);
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                                       @Override
                                       public void accept(String s) {
                                           connectedDeviceAddress.postValue(s);
                                       }
                                   },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) {
                                        connectedDeviceAddress.postValue("");
                                        throwable.printStackTrace();
                                    }
                                }
                        ));

            }
        });
    }

    public void onWifiThisDeviceChanged() {

    }

    public void onSetConnectedDeviceInfo(WifiP2pInfo connectedDevice) {
        connectedDeviceInfo = connectedDevice;

    }


    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}

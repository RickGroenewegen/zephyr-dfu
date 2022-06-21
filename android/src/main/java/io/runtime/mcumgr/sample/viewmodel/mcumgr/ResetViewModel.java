/*
 * Copyright (c) 2018, Nordic Semiconductor
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.sample.viewmodel.mcumgr;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.runtime.mcumgr.McuMgrCallback;
import io.runtime.mcumgr.McuMgrTransport;
import io.runtime.mcumgr.exception.McuMgrException;
import io.runtime.mcumgr.managers.DefaultManager;
import io.runtime.mcumgr.response.McuMgrResponse;

public class ResetViewModel extends McuMgrViewModel {
    private final DefaultManager manager;

    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    @Inject
    ResetViewModel(final DefaultManager manager,
                   @Named("busy") final MutableLiveData<Boolean> state) {
        super(state);
        this.manager = manager;
    }

    @NonNull
    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void reset() {
        setBusy();
        manager.reset(new McuMgrCallback<McuMgrResponse>() {
            @Override
            public void onResponse(@NonNull final McuMgrResponse response) {
                manager.getTransporter().addObserver(new McuMgrTransport.ConnectionObserver() {
                    @Override
                    public void onConnected() {
                        // ignore
                    }

                    @Override
                    public void onDisconnected() {
                        manager.getTransporter().removeObserver(this);
                        postReady();
                    }
                });
            }

            @Override
            public void onError(@NonNull final McuMgrException error) {
                errorLiveData.postValue(error.getMessage());
                postReady();
            }
        });
    }
}

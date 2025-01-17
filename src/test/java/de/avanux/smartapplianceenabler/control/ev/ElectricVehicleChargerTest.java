/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.avanux.smartapplianceenabler.control.ev;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ElectricVehicleChargerTest {

    private ElectricVehicleCharger evCharger = new ElectricVehicleCharger();
    private EVControl evControl = Mockito.mock(EVControl.class);

    public ElectricVehicleChargerTest() {
        evCharger.setControl(evControl);
        evCharger.setApplianceId("TEST");
        evCharger.init();
    }

    private void configureMocks(boolean vehicleNotConnected, boolean vehicleConnected, boolean charging,
                                boolean chargingCompleted) {
        Mockito.when(evControl.isVehicleNotConnected()).thenReturn(vehicleNotConnected);
        Mockito.when(evControl.isVehicleConnected()).thenReturn(vehicleConnected);
        Mockito.when(evControl.isCharging()).thenReturn(charging);
        Mockito.when(evControl.isChargingCompleted()).thenReturn(chargingCompleted);
    }

    @Test
    public void getNewState_initial() {
        Mockito.when(evControl.isVehicleConnected()).thenReturn(false);
        Assert.assertEquals(State.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(State.VEHICLE_NOT_CONNECTED));
    }

    @Test
    public void getNewState_connect() {
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        Assert.assertEquals(State.VEHICLE_CONNECTED,
                evCharger.getNewState(State.VEHICLE_NOT_CONNECTED));
    }

    @Test
    public void getNewState_charging() {
        Mockito.when(evControl.isCharging()).thenReturn(true);
        Assert.assertEquals(State.CHARGING,
                evCharger.getNewState(State.VEHICLE_CONNECTED));
    }

    @Test
    public void getNewState_disconnect() {
        Mockito.when(evControl.isVehicleNotConnected()).thenReturn(true);
        Assert.assertEquals(State.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(State.VEHICLE_CONNECTED));
    }

    @Test
    public void getNewState_disconnectWhileCharging() {
        Mockito.when(evControl.isVehicleNotConnected()).thenReturn(true);
        Mockito.when(evControl.isCharging()).thenReturn(false);
        Assert.assertEquals(State.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(State.CHARGING));
    }

    @Test
    public void wasInState() {
        Assert.assertFalse(evCharger.wasInState(State.VEHICLE_CONNECTED));
        evCharger.setState(State.CHARGING);
        Assert.assertFalse(evCharger.wasInState(State.VEHICLE_CONNECTED));
        evCharger.setState(State.VEHICLE_CONNECTED);
        Assert.assertTrue(evCharger.wasInState(State.VEHICLE_CONNECTED));
        evCharger.setState(State.CHARGING);
        Assert.assertTrue(evCharger.wasInState(State.VEHICLE_CONNECTED));
    }

    @Test
    public void wasInStateOneTime() {
        Assert.assertFalse(evCharger.wasInStateOneTime(State.VEHICLE_CONNECTED));
        evCharger.setState(State.CHARGING);
        Assert.assertFalse(evCharger.wasInStateOneTime(State.VEHICLE_CONNECTED));
        evCharger.setState(State.VEHICLE_CONNECTED);
        Assert.assertTrue(evCharger.wasInStateOneTime(State.VEHICLE_CONNECTED));
        evCharger.setState(State.CHARGING);
        Assert.assertTrue(evCharger.wasInStateOneTime(State.VEHICLE_CONNECTED));
        evCharger.setState(State.VEHICLE_CONNECTED);
        Assert.assertFalse(evCharger.wasInStateOneTime(State.VEHICLE_CONNECTED));
    }

    @Test
    public void updateState_noInterruption() {
        // VEHICLE_NOT_CONNECTED
        configureMocks(true, false, false, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_NOT_CONNECTED, evCharger.getState());
        // VEHICLE_CONNECTED
        configureMocks(false, true, false, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_CONNECTED, evCharger.getState());
        // CHARGING
        configureMocks(false, true, true, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.CHARGING, evCharger.getState());
        // CHARGING_COMPLETED
        configureMocks(false, true, false, true);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.CHARGING_COMPLETED, evCharger.getState());
        // VEHICLE_NOT_CONNECTED
        configureMocks(true, false, false, true);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_noInterruption_2cycles() {
        updateState_noInterruption();
        updateState_noInterruption();
    }

    @Test
    public void updateState_interruption()throws Exception {
        // VEHICLE_NOT_CONNECTED
        configureMocks(true, false, false, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_NOT_CONNECTED, evCharger.getState());
        // VEHICLE_CONNECTED
        configureMocks(false, true, false, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_CONNECTED, evCharger.getState());
        // CHARGING
        configureMocks(false, true, true, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.CHARGING, evCharger.getState());
        // VEHICLE_CONNECTED
        configureMocks(false, true, false, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_CONNECTED, evCharger.getState());
        evCharger.startChargingStateDetectionDelay = 2;
        evCharger.startCharging();
        while(! evCharger.updateState(0l)) {
            Thread.sleep(500);
        }
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_CONNECTED, evCharger.getState());
        // CHARGING
        configureMocks(false, true, true, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.CHARGING, evCharger.getState());
        // CHARGING_COMPLETED
        configureMocks(false, true, false, true);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.CHARGING_COMPLETED, evCharger.getState());
        // VEHICLE_NOT_CONNECTED
        configureMocks(true, false, false, true);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_initiallyConnected() {
        // VEHICLE_CONNECTED
        configureMocks(false, true, false, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_CONNECTED, evCharger.getState());
        // CHARGING
        configureMocks(false, true, true, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.CHARGING, evCharger.getState());
        // CHARGING_COMPLETED
        configureMocks(false, true, false, true);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.CHARGING_COMPLETED, evCharger.getState());
        // VEHICLE_NOT_CONNECTED
        configureMocks(true, false, false, true);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_abort() {
        // VEHICLE_NOT_CONNECTED
        configureMocks(true, false, false, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_NOT_CONNECTED, evCharger.getState());
        // VEHICLE_CONNECTED
        configureMocks(false, true, false, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_CONNECTED, evCharger.getState());
        // CHARGING
        configureMocks(false, true, true, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.CHARGING, evCharger.getState());
        // VEHICLE_NOT_CONNECTED
        configureMocks(true, false, false, true);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_error() {
        // CHARGING
        configureMocks(false, true, true, false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Mockito.when(evControl.isInErrorState()).thenReturn(true);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.ERROR, evCharger.getState());
        Mockito.when(evControl.isInErrorState()).thenReturn(false);
        evCharger.updateState(0l);
        evCharger.updateState(0l);
        Assert.assertEquals(State.CHARGING, evCharger.getState());
    }

    @Test
    public void isWithinStartChargingStateDetectionDelay() {
        int startChargingStateDetectionDelay = 300;
        long currentMillis = 1000000;
        Assert.assertFalse(evCharger.isWithinStartChargingStateDetectionDelay(
                startChargingStateDetectionDelay, currentMillis, null));
        Assert.assertTrue(evCharger.isWithinStartChargingStateDetectionDelay(startChargingStateDetectionDelay, currentMillis, currentMillis - (startChargingStateDetectionDelay - 1) * 1000));
        Assert.assertFalse(evCharger.isWithinStartChargingStateDetectionDelay(startChargingStateDetectionDelay, currentMillis, currentMillis - (startChargingStateDetectionDelay + 1) * 1000));
    }

    @Test
    public void isOn_startChargingStateDetectionDelay() {
        int startChargingStateDetectionDelay = 300;
        long currentMillis = 1000000;
        // initial state != CHARGING
        Assert.assertFalse(evCharger.isOn(
                startChargingStateDetectionDelay, currentMillis, null));
        Assert.assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay - 1) * 1000));
        Assert.assertFalse(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay + 1) * 1000));
        // state = CHARGING
        evCharger.setState(State.CHARGING);
        Assert.assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay, 0, null));
        Assert.assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay - 1) * 1000));
        Assert.assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay + 1) * 1000));
    }
}

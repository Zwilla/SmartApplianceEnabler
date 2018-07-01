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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ControlStateChangedListener;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.Timer;
import java.util.TimerTask;

@XmlAccessorType(XmlAccessType.FIELD)
public class ElectricVehicleCharger implements Control, ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(ElectricVehicleCharger.class);
    @XmlElements({
            @XmlElement(name = "EVModbusControl", type = EVModbusControl.class),
    })
    private EVControl evControl;
    private transient String applianceId;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        evControl.setApplianceId(applianceId);
    }

    public EVControl getEvControl() {
        return evControl;
    }

    public void init() {
        logger.debug("{}: Initializing ...", this.applianceId);
        evControl.validate();
    }

    public void start(Timer timer) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean vehicleConnected = evControl.isVehicleConnected();
                logger.debug("{}: vehicleConnected = {}", applianceId, vehicleConnected);
            }
        }, 0, evControl.getVehicleStatusPollInterval() * 1000);
    }

    @Override
    public boolean on(LocalDateTime now, boolean switchOn) {
        return false;
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {

    }

    @Override
    public boolean isOn() {
        return false;
    }



}

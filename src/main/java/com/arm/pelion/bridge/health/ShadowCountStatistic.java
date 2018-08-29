/**
 * @file ShadowCountStatistic.java
 * @brief Pelion bridge shadow device count statistic
 * @author Doug Anson
 * @version 1.0
 * @see
 *
 * Copyright 2015. ARM Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.arm.pelion.bridge.health;

import com.arm.pelion.bridge.coordinator.processors.arm.GenericMQTTProcessor;
import com.arm.pelion.bridge.coordinator.processors.factories.BasePeerProcessorFactory;
import com.arm.pelion.bridge.coordinator.processors.interfaces.PeerProcessorInterface;
import com.arm.pelion.bridge.health.interfaces.HealthCheckServiceInterface;
import java.util.List;

/**
 * This class periodically checks how many device shadows its established
 *
 * @author Doug Anson
 */
public class ShadowCountStatistic extends BaseValidatorClass implements Runnable {
    private int m_last_value = -1;
    
    // default constructor
    public ShadowCountStatistic(HealthCheckServiceInterface provider) {
        super(provider,"shadow_count");
        this.m_value = (Integer)0;      // Integer value for this validator
        this.m_last_value = -1;
    }   
    
    // validate
    @Override
    protected void validate() {
        // DEBUG
        this.errorLogger().info("ShadowCountStatistic: Checking device shadow count...");
        this.m_value = (Integer)this.checkDeviceShadowCount();
        
        if (this.m_last_value != (Integer)this.m_value) {
            this.m_last_value = (Integer)this.m_value;
            this.errorLogger().info("ShadowCountStatistic: Updated device shadow count: " + (Integer)this.m_value);
            this.updateStatisticAndNotify();
        }
    }

    // WORKER: query how many device shadows we currently have
    private int checkDeviceShadowCount() {
        int count = 0;
        List<PeerProcessorInterface> list = this.m_provider.getPeerProcessorList();
        for(int i=0;list != null && i<list.size();++i) {
            BasePeerProcessorFactory f = (BasePeerProcessorFactory)list.get(i);
            GenericMQTTProcessor p = f.mqttProcessor();
            if (p != null) {
                // cumulative
                count += p.getEndpointCount();
            }
        }
        return count;
    }
}
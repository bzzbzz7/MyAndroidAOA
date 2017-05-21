package com.yuneec.UsbService.adk;

import com.yuneec.UsbService.adk.respone.AdkMessage;


/**
 * Defines ADK interfaces
 */

public interface IAdkManager {
    AdkMessage read();
    void write(byte[] values);
    void write(byte value);
    void write(int value);
    void write(float value);
    void write(String text);

    // Activity related interfaces
    void close();
    void open();
}

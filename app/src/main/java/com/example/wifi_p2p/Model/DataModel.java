package com.example.wifi_p2p.Model;

import java.io.Serializable;

public class DataModel implements Serializable {
    private String filePath;
    private String FileName;
    private Long FileLength;
    private String type;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public Long getFileLength() {
        return FileLength;
    }

    public void setFileLength(Long fileLength) {
        FileLength = fileLength;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package basilliyc.chirkotestatn.entity;

public class SendMediaInfo {
    private String fileName;
    private long fileLength;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    @Override
    public String toString() {
        return "SendMediaInfo{" +
                "fileName='" + fileName + '\'' +
                ", fileLength=" + fileLength +
                '}';
    }
}

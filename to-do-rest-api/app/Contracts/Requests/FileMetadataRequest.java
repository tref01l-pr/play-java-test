package Contracts.Requests;

public class FileMetadataRequest {
    private String fileName;
    private String fileType;
    private String pdfHash;
    private int pageCount;

    public FileMetadataRequest(String fileName, String fileType, String pdfHash, int pageCount) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.pdfHash = pdfHash;
        this.pageCount = pageCount;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getHash() {
        return pdfHash;
    }

    public int getPageCount() {
        return pageCount;
    }
}

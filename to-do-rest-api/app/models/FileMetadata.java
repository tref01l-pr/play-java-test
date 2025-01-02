package models;

import dev.morphia.annotations.Embedded;
import org.bson.types.ObjectId;

import java.util.List;

@Embedded
public class FileMetadata {
    private String fileName;
    private String fileType;
    private String pdfHash;
    private List<ObjectId> imageIds;

    public FileMetadata() { }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getPdfHash() {
        return pdfHash;
    }

    public void setPdfHash(String pdfHash) {
        this.pdfHash = pdfHash;
    }

    public List<ObjectId> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<ObjectId> imageIds) {
        this.imageIds = imageIds;
    }
}

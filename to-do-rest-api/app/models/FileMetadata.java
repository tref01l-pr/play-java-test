package models;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

import java.util.List;

@Entity(value = "files", useDiscriminator = false)
public class FileMetadata {
    @Id
    private ObjectId _id;
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

    public String getHash() {
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

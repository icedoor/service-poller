package se.kry.codetest;

import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Service {

    private URL url;
    private String name;
    private long added;
    private String status;

    public Service(URL url, String name, long added) {
        this.url = url;
        this.name = name;
        this.added = added;
        status = "UNKNOWN";
    }

    public URL getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getTimestamp() {
        Date date = new Date(added);
        Format format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        return format.format(date);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

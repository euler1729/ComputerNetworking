import java.io.IOException;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Client {
    public static void main(String[] args) throws Exception {
        String fileUrl = "http://localhost:8000/a.txt";
        String fileName = "a.txt";
        URL website = new URL(fileUrl);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
    }
}

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package javasshdemo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class App {

    private static final String HOST = "dcc-login.oit.duke.edu";
    private static final String USERNAME = "xh123";
    private static final String PRIVATEKEY_PATH = "~/.ssh/id_rsa";
    private static final String KNOWNHOSTS_PATH = "~/.ssh/known_hosts";

    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) throws JSchException, IOException, SftpException, InterruptedException {
        System.out.println(new App().getGreeting());

        // testS3();

        // testSSH();

        testAIGC();

    }

    public static void testSSH() throws JSchException, IOException, SftpException, InterruptedException {
        DukeDccSshService sshService = new DukeDccSshService(HOST, USERNAME, PRIVATEKEY_PATH,
                KNOWNHOSTS_PATH);

        sshService.execCommands("whoami");
        sshService.execCommands("pwd");
        sshService.execCommands("mkdir -p test-jsch");
        sshService.sftpFromLocal("../hello-world.sh", "./test-jsch/hello-world.sh");
        // sshService.execCommands("chmod 0700 test-jsch/hello-world.sh");
        // sshService.execCommands("/opt/slurm/bin/srun test-jsch/hello-world.sh");
        // sshService.sftpFromRemote("helloworld.txt", "../helloworld.txt");

        sshService.close();
    }

    public static void testS3() throws IOException {
        AwsS3Service s3 = new AwsS3Service();
        s3.uploadObj("aipb.duke.bucket1", "test-key-1", "Hello World???".getBytes());
        byte[] bytes = s3.getObj("aipb.duke.bucket1", "test-key-1");
        System.out.println(new String(bytes));

        s3.uploadObj("aipb.duke.bucket1", "test-key-1", "!!!!Hello World???".getBytes());
        System.out.println(new String(s3.getObj("aipb.duke.bucket1", "test-key-1")));

        s3.deleteObj("aipb.duke.bucket1", "test-key-1");
    }

    public static void testAIGC() throws JSchException, SftpException, IOException {
        DukeDccSshService sshService = new DukeDccSshService(HOST, USERNAME, PRIVATEKEY_PATH,
                KNOWNHOSTS_PATH);
        DukeDccAIGCService dukeDccAIGCService = new DukeDccAIGCService(sshService, "scripts");

        // new Thread(() -> {
        // try {
        // dukeDccAIGCService.trainImageGenerationModel("testuser2", 234,
        // "scripts/dataset/*", "Zhuma");
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }).start();

        // dukeDccAIGCService.trainImageGenerationModel("testuser", 233,
        // "scripts/dataset/*", "Qingmei");

        // dukeDccAIGCService.text2image("testuser", 233,
        // "Xiaobai had two pets called Xiaohei and Bidiu in a picturesque village.",
        // "Xiaobai, Xiaohei, and Bidiu embarked on exciting adventures every
        // morning,exploring forests and enjoying the wonders of nature.");

        List<String> segments = dukeDccAIGCService.genStoryFromPrompt("task_test_user", "a dog named Max loves running",
                5);
        for (String seg : segments) {
            System.out.println(seg);
        }
    }

}

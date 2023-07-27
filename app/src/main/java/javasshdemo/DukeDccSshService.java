package javasshdemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class DukeDccSshService {

    private final JSch jsch;
    private final Session session;

    private final ByteArrayOutputStream outputBuffer;

    public DukeDccSshService(String HOST, String USERNAME, String PRIVATEKEY_PATH, String KNOWNHOSTS_PATH)
            throws JSchException {
        this.jsch = new JSch();
        this.jsch.addIdentity(PRIVATEKEY_PATH);
        this.jsch.setKnownHosts(KNOWNHOSTS_PATH);
        this.session = jsch.getSession(USERNAME, HOST);
        this.session.connect(5000);

        this.outputBuffer = new ByteArrayOutputStream();
    }

    public void execCommands(String... commands) throws JSchException, IOException {
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        try {

            channelExec.setCommand(String.join(";", commands));

            channelExec.setOutputStream(outputBuffer);
            channelExec.setErrStream(outputBuffer);

            channelExec.connect(5000);

            while (!channelExec.isClosed()) {
                Thread.sleep(1000); // waiting for commands to execution completely
            }
        } catch (RuntimeException ex) {
            System.err.println(ex.getCause().getMessage());
        } catch (InterruptedException ex) {
            System.err.println(ex.getCause().getMessage());
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
            String msg = outputBuffer.toString();
            if (!msg.isEmpty()) {
                System.out.println(outputBuffer.toString());
            }
            outputBuffer.reset();
        }
    }

    public void sftpFromLocal(String sourceAbsDir, String destAbsDir) throws JSchException, SftpException {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        try {
            channelSftp.connect(5000);
            channelSftp.put(sourceAbsDir, destAbsDir);
        } catch (RuntimeException ex) {
            System.err.println(ex.getCause().getMessage());
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
        }
    }

    public void sftpFromRemote(String sourceAbsDir, String destAbsDir) throws JSchException, SftpException {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        try {
            channelSftp.connect(5000);
            channelSftp.get(sourceAbsDir, destAbsDir);
        } catch (RuntimeException ex) {
            System.err.println(ex.getCause().getMessage());
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
        }
    }

    public void close() {
        if (session != null) {
            session.disconnect();
        }
    }

}

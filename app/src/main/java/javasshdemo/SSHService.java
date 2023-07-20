package javasshdemo;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SSHService {

    private final JSch jsch;
    private final Session session;

    public SSHService(String HOST, String USERNAME, String PRIVATEKEY_PATH, String KNOWNHOSTS_PATH)
            throws JSchException {
        this.jsch = new JSch();
        this.jsch.addIdentity(PRIVATEKEY_PATH);
        this.jsch.setKnownHosts(KNOWNHOSTS_PATH);
        this.session = jsch.getSession(USERNAME, HOST);
        this.session.connect(5000);
    }

    public void execCommands(String... commands) throws JSchException, InterruptedException {
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        try {
            channelExec.setCommand(String.join(";", commands));
            channelExec.connect(5000);
            while (!channelExec.isClosed()) {
                Thread.sleep(1000);
            }
        } catch (RuntimeException ex) {
            System.out.println(ex.getCause().getMessage());
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
    }

    public void sftpFromLocal(String sourceAbsDir, String destAbsDir) throws JSchException, SftpException {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        try {
            channelSftp.connect(5000);
            channelSftp.put(sourceAbsDir, destAbsDir);
        } catch (RuntimeException ex) {
            System.out.println(ex.getCause().getMessage());
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
            System.out.println(ex.getCause().getMessage());
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

package javasshdemo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class DukeDccAIGCService {

        private final DukeDccSshService sshService;

        private final String localScriptsDir;

        public DukeDccAIGCService(DukeDccSshService sshService, String localScriptsDir) {
                this.sshService = sshService;
                this.localScriptsDir = localScriptsDir;
        }

        public void trainImageGenerationModel(String username, long storyID, String dataSetPath, String valPrompt)
                        throws JSchException, SftpException, IOException {
                String remoteDir = username + "_" + storyID + "_train";
                String localDir = "fine_tuned_model/" + username + "_" + storyID;
                sshService.execCommands("mkdir -p " + remoteDir);
                sshService.execCommands("mkdir -p " + remoteDir + "/output");
                sshService.execCommands("mkdir -p " + remoteDir + "/dataset");
                sshService.sftpFromLocal(localScriptsDir + "/dataset/*", remoteDir + "/dataset");
                sshService.sftpFromLocal(localScriptsDir + "/train_model.sh", remoteDir);
                sshService.execCommands("export OUTPUT_DIR=\"" + remoteDir + "/output\"");
                sshService.execCommands("export DATASET_NAME=\"" + remoteDir + "/dataset\"");
                sshService.execCommands("export VAL_PROMPT=\"" + valPrompt + "\"");
                sshService.execCommands("chmod 0711 " + remoteDir + "/train_model.sh");
                sshService.execCommands("/opt/slurm/bin/srun -p gpu-common -c 10 --gres=gpu:RTXA5000:1 --mem=16G " +
                                remoteDir + "/train_model.sh");
                Files.createDirectories(Paths.get(localDir));
                sshService.sftpFromRemote(remoteDir + "/output/pytorch_lora_weights.bin", localDir);
        }

        public void text2image(String username, long storyID, String imgPrompt)
                        throws JSchException, SftpException, IOException {
                String remoteDir = username + "_" + storyID + "_inference";
                String localDir = "fine_tuned_model/" + username + "_" + storyID;
                sshService.execCommands("mkdir -p " + remoteDir);
                sshService.execCommands("mkdir -p " + remoteDir + "/output");
                sshService.sftpFromLocal(localScriptsDir + "/inference.sh", remoteDir);
                sshService.sftpFromLocal(localDir + "/pytorch_lora_weights.bin", remoteDir);
                sshService.execCommands("export MODEL_PATH=\"" + remoteDir + "\"");
                sshService.execCommands("export OUTPUT_DIR=\"" + remoteDir + "/output\"");
                sshService.execCommands("echo \"" + imgPrompt + "\" > " + remoteDir + "/prompt.txt");
                sshService.execCommands("export PROMPT_PATH=\"" + remoteDir + "/prompt.txt\"");
                sshService.execCommands("chmod 0711 " + remoteDir + "/inference.sh");
                sshService.execCommands("/opt/slurm/bin/srun -p gpu-common --gres=gpu:RTXA5000:1 --mem=16G "
                                + remoteDir + "/inference.sh");
                Files.createDirectories(Paths.get(localDir + "/images"));
                sshService.sftpFromRemote(remoteDir + "/output/0.png",
                                localDir + "/images");
        }

}

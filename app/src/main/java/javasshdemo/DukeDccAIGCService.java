package javasshdemo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
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
                sshService.execCommands(
                                "mkdir -p " + remoteDir,
                                "mkdir -p " + remoteDir + "/output",
                                "mkdir -p " + remoteDir + "/dataset");
                sshService.sftpFromLocal(localScriptsDir + "/dataset/*", remoteDir + "/dataset");
                sshService.sftpFromLocal(localScriptsDir + "/train_model.sh", remoteDir);
                sshService.execCommands(
                                "export OUTPUT_DIR=\"" + remoteDir + "/output\"",
                                "export DATASET_NAME=\"" + remoteDir + "/dataset\"",
                                "export VAL_PROMPT=\"" + valPrompt + "\"",
                                "chmod 0711 " + remoteDir + "/train_model.sh",
                                "/opt/slurm/bin/srun -p gpu-common -c 10 --gres=gpu:RTXA5000:1 --mem=16G " +
                                                remoteDir + "/train_model.sh");
                Files.createDirectories(Paths.get(localDir));
                sshService.sftpFromRemote(remoteDir + "/output/pytorch_lora_weights.bin", localDir);
        }

        public void text2image(String username, long storyID, String... imgPrompts)
                        throws JSchException, SftpException, IOException {
                String remoteDir = username + "_" + storyID + "_inference";
                String localDir = "fine_tuned_model/" + username + "_" + storyID;
                int n = imgPrompts.length;
                String promptsText = String.join("\n", imgPrompts);
                sshService.execCommands(
                                "mkdir -p " + remoteDir,
                                "mkdir -p " + remoteDir + "/output");
                sshService.sftpFromLocal(localScriptsDir + "/inference.sh", remoteDir);
                sshService.sftpFromLocal(localDir + "/pytorch_lora_weights.bin", remoteDir);
                sshService.execCommands(
                                "export MODEL_PATH=\"" + remoteDir + "\"",
                                "export OUTPUT_DIR=\"" + remoteDir + "/output\"",
                                "echo -e \"" + promptsText + "\" > " + remoteDir + "/prompt.txt",
                                "export PROMPT_PATH=\"" + remoteDir + "/prompt.txt\"",
                                "chmod 0711 " + remoteDir + "/inference.sh",
                                "echo $PATH",
                                "module load Anaconda3/2021.05",
                                "/opt/slurm/bin/srun -p gpu-common --gres=gpu:RTXA5000:1 --mem=16G " + remoteDir
                                                + "/inference.sh");
                Files.createDirectories(Paths.get(localDir + "/images"));
                for (int i = 0; i < n; i++) {
                        sshService.sftpFromRemote(remoteDir + "/output/" + i + ".png",
                                        localDir + "/images");
                }
        }

        public List<String> genStoryFromPrompt(String taskTag, String prompt, int num)
                        throws JSchException, IOException, SftpException {

                final String storyGenScriptPath = "scripts_llama2/Llama2.py";

                String localDir = new String("llama2/" + taskTag).replaceAll("\\s+", "");
                Files.createDirectories(Paths.get(localDir));

                String remoteDir = new String("llama2/" + taskTag).replaceAll("\\s+", "");

                sshService.execCommands("mkdir -p " + remoteDir);
                sshService.sftpFromLocal(storyGenScriptPath, remoteDir);
                sshService.execCommands(
                                // "chmod 0711 " + remoteDir + "/generate_story.sh",
                                "export OUTPUT_DIR=" + remoteDir + "/",
                                "/opt/slurm/bin/srun -p gpu-common --gres=gpu:RTXA5000:1 python3 "
                                                + remoteDir + "/Llama2.py " + "\"" + prompt + "\" " + num);
                sshService.sftpFromRemote(remoteDir + "/storyyyy.json", localDir);

                byte[] jsonData = Files.readAllBytes(Paths.get(localDir + "/storyyyy.json"));
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> stringList = objectMapper.readValue(jsonData, List.class);

                sshService.execCommands("rm -rf " + remoteDir);

                return stringList;
        }

}

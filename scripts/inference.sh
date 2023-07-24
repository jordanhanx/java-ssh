#!/bin/bash
#srun -p gpu-common --gres=gpu:1 --mem=16G --pty bash -i
module load Anaconda3/2021.05
export MODEL_PATH="/hpc/group/pfisterlab/xc139/childbook/text2img/LuoXiaoHei01"
export PROMPT_PATH="/hpc/group/pfisterlab/for-lora/example_in/prompt_examples.txt"
export OUTPUT_DIR="try_inference_out"

python /hpc/group/pfisterlab/for-lora/script/inference.py \
  --pretrained_model_path=$MODEL_PATH \
  --prompt_path=$PROMPT_PATH \
  --output_dir=$OUTPUT_DIR
  
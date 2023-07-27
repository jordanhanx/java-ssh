#!/bin/bash
#srun -p gpu-common -c 10 --gres=gpu:1 --mem=16G --pty bash -i
module load Anaconda3/2021.05
# export OUTPUT_DIR="Hongdou001"
# export DATASET_NAME="/hpc/group/pfisterlab/for-lora/training-data/Hongdou"
# export VAL_PROMPT="Qingmei"
export MAX_TRAIN_STEPS=500

export DISABLE_TELEMETRY=YES
accelerate config default
accelerate launch --mixed_precision="fp16"  /hpc/group/pfisterlab/for-lora/script/train_text_to_image_lora.py \
  --pretrained_model_name_or_path="runwayml/stable-diffusion-v1-5" \
  --train_data_dir=$DATASET_NAME \
  --dataloader_num_workers=8 \
  --resolution=512 --center_crop --random_flip \
  --train_batch_size=1 \
  --gradient_accumulation_steps=4 \
  --max_train_steps=$MAX_TRAIN_STEPS \
  --learning_rate=1e-4 \
  --max_grad_norm=1 \
  --lr_scheduler="cosine" --lr_warmup_steps=0 \
  --output_dir=${OUTPUT_DIR} \
  --checkpointing_steps=500 \
  --validation_prompt=$VAL_PROMPT \
  --validation_epochs=100 \
  --seed=1029\
  --caption_column="additional_feature"
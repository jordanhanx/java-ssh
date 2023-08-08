from huggingface_hub import login
login(token = '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')
import torch
import transformers
from transformers import AutoTokenizer, AutoModelForCausalLM
import sys
import json
import os
import textwrap

def load_tokenizer():
    tokenizer = AutoTokenizer.from_pretrained("meta-llama/Llama-2-7b-chat-hf",
                                              use_auth_token=True,return_token_type_ids=False)
    return tokenizer

def load_model():
    model = AutoModelForCausalLM.from_pretrained("meta-llama/Llama-2-7b-chat-hf",
                                                 device_map='auto',
                                                 torch_dtype=torch.float16,
                                                 use_auth_token=True,
                                                 cache_dir='/work/za53'
                                                #  load_in_8bit=True,
                                                #  load_in_4bit=True
                                                 )
    return model

def get_prompt(instruction, DEFAULT_SYSTEM_PROMPT):
    B_INST, E_INST = "[INST]", "[/INST]"
    B_SYS, E_SYS = "<<SYS>>\n", "\n<</SYS>>\n\n"
    SYSTEM_PROMPT = B_SYS + DEFAULT_SYSTEM_PROMPT + E_SYS
    prompt_template =  B_INST + SYSTEM_PROMPT + instruction + E_INST
    return prompt_template

def cut_off_text(text, prompt):
    cutoff_phrase = prompt
    index = text.find(cutoff_phrase)
    if index != -1:
        return text[:index]
    else:
        return text

def remove_substring(string, substring):
    return string.replace(substring, "")



def generate(text, DEFAULT_SYSTEM_PROMPT, model, tokenizer):
    prompt = get_prompt(text, DEFAULT_SYSTEM_PROMPT)
    with torch.autocast('cuda', dtype=torch.bfloat16):
        inputs = tokenizer(prompt, return_tensors="pt").to('cuda')
        outputs = model.generate(**inputs,
                                 max_new_tokens=2048,
                                 temperature=1,
                                 top_p = 0.95,
                                 top_k = 50,
                                 eos_token_id=tokenizer.eos_token_id,
                                 pad_token_id=tokenizer.eos_token_id,
                                 )
        final_outputs = tokenizer.batch_decode(outputs, skip_special_tokens=True)[0]
        final_outputs = cut_off_text(final_outputs, '</s>')
        final_outputs = remove_substring(final_outputs, prompt)

    return final_outputs#, outputs

def generate_story(text, model, tokenizer, num):
    DEFAULT_SYSTEM_PROMPT = "Below is a summary of a children's story. Write a full story more than 1000 words. " +  \
                            "Make sure your story contains " + num + " paragraphs, with each paragraph separated by [ENDING]." + \
                            "Make sure your story follows the same plot points of the summary and expands on each point. " + \
                            "Make sure your story has a clear beginning, multiple plot points that build on each other, and a clear ending. "
    return generate(text, DEFAULT_SYSTEM_PROMPT, model, tokenizer)


def parse_text(text):
        wrapped_text = textwrap.fill(text, width=100)
        return wrapped_text
        # return assistant_text

def split_string(input_string):
    s_list = input_string.split('[ENDING]')
    return s_list

def remove_unnecessary_text(input_string):
    index = input_string.find(":")
    end_index = input_string.find('[ENDING]')
    if index != -1 and index < end_index:
        if 'here' in input_string.lower()[:index]:
            result_string = input_string[index+1:]
        else:
            result_string = input_string
    else:
        result_string = input_string

    return result_string


def get_full_story(wrapped_text):
    full_story = []
    wrapped_text = wrapped_text.replace('  ', ' ')
    for x in split_string(wrapped_text.replace('\n', ' ')):
        full_story.append(' ' + x + '\n')
    return full_story
    
def create_story(prompt, model, tokenizer, num):
    generated_text = generate_story(prompt, model, tokenizer, num)
    wrapped_text = parse_text(generated_text)
    wrapped_text = remove_unnecessary_text(wrapped_text)
    full_story = []
    full_story = get_full_story(wrapped_text)
    return wrapped_text, full_story

def save_story(full_story):
    # Get the OUTPUT_DIR from the environment variable
    output_dir = os.environ.get("OUTPUT_DIR", ".")
    
    # Convert the list to a dictionary with keys as paragraph numbers
    # story_dict = {f"paragraph_{i+1}": paragraph.strip() for i, paragraph in enumerate(full_story)}

    # Save the dictionary as a JSON file in the specified OUTPUT_DIR
    with open(os.path.join(output_dir, "storyyyy.json"), "w") as json_file:
        json.dump(full_story, json_file, indent=4)


if __name__ == "__main__":
    # Check if the number of command-line arguments is correct
    if len(sys.argv) != 2:
        print("Usage: python Llama2.py <prompt> <paragraphs-num>")
        sys.exit(1)

    prompt = sys.argv[1]
    num =  sys.argv[2]
    model = load_model()
    tokenizer = load_tokenizer()

    # Call the function to create the story
    wrapped_text, full_story = create_story(prompt, model, tokenizer, num)
    save_story(full_story)
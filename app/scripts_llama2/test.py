import json
import os

def save_story(full_story):
    # Get the OUTPUT_DIR from the environment variable
    output_dir = os.environ.get("OUTPUT_DIR", ".")
    
    # Convert the list to a dictionary with keys as paragraph numbers
    # story_dict = {f"paragraph_{i+1}": paragraph.strip() for i, paragraph in enumerate(full_story)}

    # Save the dictionary as a JSON file in the specified OUTPUT_DIR
    with open(os.path.join(output_dir, "storyyyy.json"), "w") as json_file:
        json.dump(full_story, json_file, indent=4)
        
        
if __name__ == "__main__":
    
     save_story(["Max1 was a dog who loved running.", "Max2 was a dog who loved running.", "Max3 was a dog who loved running."])
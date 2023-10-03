import cv2
import os
import argparse

def images_to_video(image_folder, video_name, fps):
    images = sorted([img for img in os.listdir(image_folder) if img.endswith(".jpg")])
    frame = cv2.imread(os.path.join(image_folder, images[0]))
    h, w, layers = frame.shape
    video = cv2.VideoWriter(video_name, cv2.VideoWriter_fourcc(*'DIVX'), fps, (w,h))

    for image in images:
        video.write(cv2.imread(os.path.join(image_folder, image)))

    cv2.destroyAllWindows()
    video.release()

def main():
    parser = argparse.ArgumentParser(description='Create a video from images in a specified directory.')
    parser.add_argument('image_folder', type=str, help='Path to the folder containing images')
    parser.add_argument('video_name', type=str, help='Name of the output video file')
    parser.add_argument('--fps', type=int, default=30, help='Frames per second (default: 30)')

    args = parser.parse_args()
    images_to_video(args.image_folder, args.video_name, args.fps)

if __name__ == "__main__":
    main()

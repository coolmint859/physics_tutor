# Interactive Physics Tutor

Within this repository contains the code created for my final project in CS 5620, AI in Education. I took this class in the Spring of 2025.

The report for this project detailing my design decisions and influences can be found here:
https://docs.google.com/document/d/1hsbeyTChycOnYgdyOdosSBh-L19p0H36YEBkZuE-9-w/edit?tab=t.0

To launch the program, all you need to do is run the file "src/PhysicsTutor.java". This
project was designed in IntelliJ 2024.3.5 (Community Edition), however it should work in any IDE 
that supports JDK 21 and the JVM. 

This project uses ChatGPT to handle the intelligence component. You will need to specify an API key for this
functionality to work. If you don't specify an API key, the program will still run, but no hints or responses will be 
generated for any of the example problems. To specify an API key, it is as simple as including it as a program argument in the following format:

```API_KEY=YOUR_KEY_GOES_HERE```

If you're running this program on macOS, you may also need to add the following command as a VM Option:

```-XstartOnFirstThread```

The project uses JBox2D, LWJGL, GSon, and a graphics library designed by Dr. Dean Mathias to work. All of these
dependencies are included in the project files.

If you have any trouble, leave me a note in the repository, and I'll work to find a solution when I have time.

Thanks!

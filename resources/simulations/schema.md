## Simulation Files

This directory contains the data that is used to render physics simulations. This allows the 
main program to be data driven, in that adding a new simulation does not require modifying the code. 
There are two main kinds of files, the index file and the simulation file. Both are in json format, and are converted
into java objects using the Gson library.

Three predefined simulations are available, "box_wedge.json", "cannon_ball.json", and "billiards.json". 
Take a look at these for examples on the format of a simulation file.

### Index

This file stores the paths to the individual simulations. Paths are represented as a java ArrayList of Strings.

When adding a simulation file, the index file will need to be updated as well. If the index file is not updated, the 
parser will not see the file and the simulation that is added will not be recognized. On the other hand, if the index
has a path to a simulation file that doesn't exist, an error will be thrown. 

### Simulation Data

The simulation data files store information about individual simulations. Here are the required parameters:

- name (String): the name of the simulation
- description (String): the description of the simulation. This will be displayed to the student and fed into the LLM, so this need to be
  fairly specific and clear. Make sure to specify any constants that the student will need to solve the problem.
- solutionOptions: (List of Strings): The possible options that the student can choose to answer the problem. I trust that at least one of these is the correct one!
- simulationTime (float): the amount of time in seconds that the simulation will play once started
- gravity (Vector2f): the force of gravity in the world, in m/s^2. (set this to 0 if birds eye view).
- bgColor (Color): the rgb colors that the graphics engine will draw to the background.
- zoom (float): the size of a meter for the graphics engine to render objects as. Smaller values effectively "zooms out", hence the name.
  - Note: a zoom value of 1 means that 1 graphics canvas unit is the same as 1 meter. As the canvas is usually from -1 to 1 in the x direction, 
    most objects will fill the screen if zoom=1. In practice, most zoom values will be fairly small (between 0.01 and 0.1).
  - The zoom is always relative to the origin, which is at the center of the screen.
- physicsObjects: a list of objects in the physics world. A physics object has the following required properties:
  - shape (String): the shape of the object (rectangle, circle, triangle, polygon)
  - bodyType (BodyType): defines how the physics engine treats the object, must be in all caps (DYNAMIC, STATIC, KINEMATIC).
  - position (Vector2f): the position of the object (it's center), in meters
  - density (float): the density of the object in kg/m^2
  - rotation (float): the amount of initial rotation that the object will have, in degrees.
  - friction (float): the "smoothness" of the object (value between 0 and 1, with 0 being perfectly smooth). 
    When two objects collide, the geometric mean is used to determine the coefficient of friction of the two objects in contact.
  - restitution (float): the "bounciness" of the object (value between 0 and 1, with 0 being not bouncy)

For graphics, physics objects also require the following parameters:
- render_z (float): The order for which the object should be rendered. Higher values mean that it gets rendered "on top of" objects with lower values.
- color (Color): The color of the object, in terms of r,g,b and a, where a is the alpha transparency. Values are between 0 and 1, where (0,0,0,0) is 
  black with no transparency.

Rectangle Physics Objects also have an optional "texture" parameter, which is a path to a image to overlay on the object.
This complements the color parameter, which gives a "tint" to the object. If the color is white (1, 1, 1),
there will be no tint on the object. Only rectangles support this feature. If a texture is added to a different object type,
it is ignored and only the color is used.

Notes:
- For polygons and triangles, one additional parameter is required, "vertices", which is a list of vertices (Vector2f) that defines 
the shape of the object (in meters). Note that the vertices should be defined **relative to the origin** and **in counter-clockwise order**. 
This setup simplifies the creation of the objects during runtime. Note that this also means that position data is baked into the vertices, 
so specifying the position parameter for a polygon will be ignored. Triangles defined with more than 3 vertices will only use the first 3.
- For rects, two additional parameters are required, "length" and "width" (both are floats). These define the bounds of the rect, and are in terms of
meters.
- For circles, one additional parameter is required, "radius" (a float), which is also in terms of meters.

When the object is defined, the mass of the object is inferred from its density and area using the formula m = d*a. This is to allow for the simulation
to work with JBox2D more easily. Objects with variable density is not supported.
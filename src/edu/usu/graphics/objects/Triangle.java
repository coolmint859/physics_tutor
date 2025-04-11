/*
Copyright (c) 2024 James Dean Mathias

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package edu.usu.graphics.objects;

import org.joml.Vector3f;

public class Triangle implements Clickable {
    public Vector3f pt1;
    public Vector3f pt2;
    public Vector3f pt3;

    public Triangle(Vector3f pt1, Vector3f pt2, Vector3f pt3) {
        this.pt1 = pt1;
        this.pt2 = pt2;
        this.pt3 = pt3;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        float x1 = pt1.x;
        float y1 = pt1.y;
        float x2 = pt2.x;
        float y2 = pt2.y;
        float x3 = pt3.x;
        float y3 = pt3.y;

        // Calculate barycentric coordinates
        float denominator = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);
        if (denominator == 0) {
            return false; // Triangle is degenerate (a line or a point)
        }

        float a = (float)((y2 - y3) * (mouseX - x3) + (x3 - x2) * (mouseY - y3)) / denominator;
        float b = (float)((y3 - y1) * (mouseX - x3) + (x1 - x3) * (mouseY - y3)) / denominator;
        float c = 1 - a - b;

        // Check if the point lies within the triangle
        return a >= 0 && a <= 1 && b >= 0 && b <= 1 && c >= 0 && c <= 1;
    }
}

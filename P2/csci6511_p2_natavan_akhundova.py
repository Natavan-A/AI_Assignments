import csv, sys, math, time
import matplotlib.pyplot as plt
import numpy as np

# additional variables
SLEEP_TIME = 0							# time between executing subsequent steps
INF = int(0x3f3f3f3f)					# infinity value
V = -1									# the number of vertices, default -1
K = -1									# the number of colors, default -1

# Main components for storage
vertices = {}							# stores vertice to color relationship
edges = {}								# stores vertice to vertice relationship

# A Function for Reading File Inputs
def read_input(filename):
	print("Reading file inputs...")
	time.sleep(SLEEP_TIME)

	global V, K # reference to global variables
	file = open(filename, 'r')
	lines = file.readlines()

	for line in lines:
		if line[0] == "#": # if a line is a comment
			continue
		line = line.strip()
		if K == -1:			# if the number of colors is not set
			K = int(line.split("=")[1].strip()) # set the number of colors
		else:
			line = line.split(",")
			from_vertice_id = int(line[0].strip())
			to_vertice_id = int(line[1].strip())

			# Vertice Assignment
			# default color for each vertex is -1
			vertices[from_vertice_id] = {"assigned":-1, "available":[c for c in range(K)]}
			vertices[to_vertice_id] = {"assigned":-1, "available":[c for c in range(K)]}

			# Edge Assignment
			# make edges adjacent in both ways since edges are undirected
			if from_vertice_id not in edges:
				edges[from_vertice_id] = []
			edges[from_vertice_id].append(to_vertice_id)

			if to_vertice_id not in edges:
				edges[to_vertice_id] = []
			edges[to_vertice_id].append(from_vertice_id)

	V = len(vertices) # set the number of vertices

	print("Done reading file inputs.")
	time.sleep(SLEEP_TIME)

# A Function to Check Whether The Current Assignment of Color Is Safe To Make:
#							(adjacent vertices should not be the same color)
def is_safe(adj_vertices, color):
	for adj_v in adj_vertices:
		# if adjacent vertex has the same color
		if vertices[adj_v]["assigned"] == color: 
			return False

	return True

# A Recursive Helping Function for Coloring the Graph - Backtracking Search
def color_graph_recursive(v, keys):
	# if all vertices have been colored
	if v == V:
		return True

	v_id = keys[v] # get id of the vertex
	adj_vertices = edges[v_id]

	# find a color in range that is safe to assign
	for color in range(K):
		if is_safe(adj_vertices, color):
			vertices[v_id]["assigned"] = color # assign the color

			if color_graph_recursive(v+1, keys): # recursively color other vertices
				return True

			vertices[v_id]["assigned"] = -1 # remove assignment if it lead to no solution

	# no color has been assigned to the vertex v
	return False


# A Function for Finding a Solution to "Coloring the Graph" Problem
def color_graph():
	keys = list(vertices.keys())
	solution = color_graph_recursive(0, keys) # start from the first vertex

	if solution == False:
		print("Solution does not exist")
	else:
		for v_id in keys:
			print("Color of %d is %d"%(v_id, vertices[v_id]["assigned"]))

# The Main Function
if __name__ == "__main__":
	# read inputs
	read_input(sys.argv[1])

	# call search function to color the map
	color_graph()
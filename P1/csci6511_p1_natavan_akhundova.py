import csv, sys, math, time
import matplotlib.pyplot as plt
import numpy as np

# additional variables
SLEEP_TIME = 1							# time between executing subsequent steps
INF = int(0x3f3f3f3f)					# infinity value
BOARD_ROW_SIZE = BOARD_COL_SIZE = 10	# rows and columns in the board
CELL_SIZE = 100							# size of one cell in the board
size_of_vertices = 0					# the number of vertices in the board, default 0
source = destination = INF 				# source and destination vertices, default infinity
distance_coefficient = 1				# coefficient to manipulate value of distances between edges

# Main components for storage
vertices = {}							# stores vertice to square relationship
edges = {}								# stores vertice to vertice relationship

# A Function for Reading File Inputs
def read_input(filename):
	print("Reading file inputs...")
	time.sleep(SLEEP_TIME)

	global size_of_vertices, source, destination, distance_coefficient # reference to global variables
	with open(filename, newline='') as csvfile:
	     csv_reader = csv.reader(csvfile, delimiter=',', quotechar='#')

	     # Vertices
	     headers = next(csv_reader)
	     row = next(csv_reader)
	     while len(row) > 0:
	     	vertice_id = int(row[0])
	     	square_id = int(row[1])
	     	vertices[vertice_id] = square_id # assign square ids to vertices
	     	row = next(csv_reader)
	     size_of_vertices = len(vertices)
	     print("Read vertices ✓")
	     time.sleep(SLEEP_TIME)

	     # Edges
	     headers = next(csv_reader)
	     row = next(csv_reader)
	     avg_distance = row_count = 0
	     while len(row) > 0:
	     	from_vertice_id = int(row[0])
	     	to_vertice_id = int(row[1])
	     	if from_vertice_id not in vertices or to_vertice_id not in vertices:
	     		raise Exception("Sorry, vertice id does not exist")

	     	distance = float(row[2])

	     	avg_distance += distance
	     	row_count +=1
	     	if from_vertice_id not in edges: 	# assign distances to edges
	     		edges[from_vertice_id] = []
	     	edges[from_vertice_id].append([to_vertice_id, distance])

	     	if to_vertice_id not in edges: 		# reverse is also true
	     		edges[to_vertice_id] = []
	     	edges[to_vertice_id].append([from_vertice_id, distance])
	     	row = next(csv_reader)

	     print("Read edges ✓")
	     time.sleep(SLEEP_TIME)

	     if row_count > 0:					# find average distance of edges
	     	avg_distance //= row_count

	     print("Recalculating edges...")
	     distance_coefficient = avg_distance//BOARD_ROW_SIZE
	     if distance_coefficient > CELL_SIZE:	#if average distance is bigger than cell size, shrink distances
	     	for e in edges:
	     		for link in edges[e]:
	     			link[1] /= distance_coefficient
	     else:
	     	distance_coefficient = 1
	     print("Done ✓")
	     time.sleep(SLEEP_TIME)

	     # Source and Destination
	     headers = next(csv_reader)[0].split('\n')
	     source = int(headers[1].split(',')[1])
	     if source == INF or source not in vertices:
	     		raise Exception("Sorry, source vertice does not exist")
	     destination = int(headers[2].split(',')[1])
	     if destination == INF or destination not in vertices:
	     		raise Exception("Sorry, destination vertice does not exist")
	     print("Read source and destination vertices ✓")
	     time.sleep(SLEEP_TIME)

	     print("Done reading file inputs.")
	     time.sleep(SLEEP_TIME)

# A Function for Calculating Euclidean Distance Between Two Points
def calculate_euclidean_distance(start, end):
	start_col_position = vertices[start]%BOARD_COL_SIZE
	start_row_position = vertices[start]//BOARD_ROW_SIZE
	end_col_position = vertices[end]%BOARD_COL_SIZE
	end_row_position = vertices[end]//BOARD_ROW_SIZE
	return int(math.dist([start_row_position, start_col_position], [end_row_position, end_col_position]))

# A Function for Printing The Shortest Path
def print_shortest_path(path):
	if len(path) > 0:
		print("\tThe shortest path:")
		for i in range(len(path)):
			if i < len(path)-1:
				print(path[i], end=" -> ")
			else:
				print(path[i])

# A Function for Showing The Contour Plot
def show_graph(visited_vertices, search_flag):
	# define an empty 10x10 2D “chess” board, consisting of 100 “squares”
	x_vals = np.arange(0, BOARD_COL_SIZE, 1)
	y_vals = np.arange(0, BOARD_ROW_SIZE*10, 10)
	X, Y = np.meshgrid(x_vals, y_vals)

	# A 10x10 2D array for height of contour
	Z = [[0 for col in range(BOARD_COL_SIZE)] for row in range(BOARD_ROW_SIZE)]
	# filling height of contour depending on square ids of visited vertices
	for i in range(len(visited_vertices)):
		if visited_vertices[i] == 1:
			square_id = vertices[i]
			row_pos = square_id//BOARD_ROW_SIZE
			col_pos = square_id%BOARD_COL_SIZE
			Z[row_pos][col_pos] += 1

	fig, ax = plt.subplots()
	ax.contourf(X, Y, Z) 				# produce a filled contour plot
	ax.xaxis.grid(True)					# define details: gridlines,
	ax.yaxis.grid(True)
	if search_flag == 1:				#				title,
		ax.set_title('Visited ID Graph: Informed Search')
	else:
		ax.set_title('Visited ID Graph: Uniformed Search')
	ax.set_xlabel('Board Columns') 		#				x_label,
	ax.set_ylabel('Board Rows') 		#				y_label

	plt.show()							# show the graph


# A Function for Implementing Uninformed Search - Dijkstra Algorithm
def uninformed_search():
	print("\033[1mImplementing Uninformed Search...\033[0m")
	parent = [-1]*size_of_vertices				# contains a parent vertex of a visited vertex
	path = []									# contains the shortest path
	visited_vertices = [0]*size_of_vertices 	# is 1 if the shortest distance from source to vertex is already found
	shortest_distances = [INF]*size_of_vertices # contains the shortest distance from source to vertex
	shortest_distances[source] = 0 				# the shortest distance is 0 for the source vertex
	visited_vertices_count = 0					# for counting visited vertices while searching the destination

	print("\tOrder of visited vertices:")
	for i in vertices:
		min_distance = INF  				# minimum distance for the current loop
		current_vertex = -1 				# current vertex starts from non-existent vertex id
		for j in vertices:
			# if a new vertex with a short distance is found
			if visited_vertices[j] == 0 and shortest_distances[j] < min_distance:
				min_distance = shortest_distances[j]
				current_vertex = j

		# no more vertices to add or the destination vertex is found
		if current_vertex < 0 or current_vertex == destination:
			if current_vertex >= 0:
				print(current_vertex)
				visited_vertices[current_vertex] = 1
				current = current_vertex

				# store the shortest path
				while current is not source:
					path.append(current)
					current = parent[current]
				path.append(source)
			break

		print(current_vertex, end=" ")	# printing each visited vertex
		# relax edges from the current vertex
		for e in edges[current_vertex]:
			v_id = e[0]
			v_distance = e[1]
			if visited_vertices[v_id] == 0:
				if shortest_distances[current_vertex]+v_distance < shortest_distances[v_id]:
					parent[v_id] = current_vertex
					shortest_distances[v_id] = shortest_distances[current_vertex] + v_distance

		# the shortest distances found from the current vertex
		visited_vertices[current_vertex] = 1
		visited_vertices_count+=1

	# result
	if shortest_distances[destination] == INF:
		print("\n\tThe path was not found")
	else:
		print("\tThe number of visited vertices is \033[1m%d\033[0m." % (visited_vertices_count+1))
		print("\tThe cost of the shortest path from %d to %d for uninformed search is \033[1m%.2f\033[0m." % (source, destination, 
																		shortest_distances[destination]*distance_coefficient))
	return (path[::-1], visited_vertices) # return a reversed path and visited vertices

# A Function for Implementing Informed Search - A* Algorithm
def informed_search():
	print("\033[1mImplementing Informed Search...\033[0m")
	parent = [-1]*size_of_vertices				# contains a parent vertex of a visited vertex
	path = []									# contains the shortest path
	G = [INF]*size_of_vertices 					# distance between the current vertex and the start vertex
	F = [INF]*size_of_vertices 					# total cost of the vertex
	G[source]=F[source] = 0 					# F cost of the source vertex is 0
	visited_vertices = [0]*size_of_vertices 	# for graph: is 1 if vertex is visited
	visited_vertices_count = 0					# for counting visited vertices while searching the destination

	open_list = []						# contains vertices yet to visit
	closed_list = set()					# contains vertices already visited
	open_list.append(source)			# add a source vertex to visit first

	print("\tOrder of visited vertices:")
	while len(open_list) > 0:
		current_vertex = open_list[0]
		current_index = 0

		# look for the lowest F score in the open list
		for i in range(len(open_list)):
			v = open_list[i]
			if F[v] < F[current_vertex]:
				current_vertex = v
				current_index = i

		# switch the vertex to the closed list
		open_list.pop(current_index)
		closed_list.add(current_vertex)

		# the destination vertex is found
		if current_vertex == destination:
			print(current_vertex)
			visited_vertices[current_vertex] = 1
			current = current_vertex

			# store the shortest path
			while current is not source:
				path.append(current)
				current = parent[current]
			path.append(source)
			
			break

		print(current_vertex, end=" ") 	# printing each visited vertex
		visited_vertices[current_vertex] = 1
		visited_vertices_count += 1
		
		# find linked vertices of the current vertex
		linked_vertices = edges[current_vertex]

		for v in linked_vertices:
			v_id = v[0]
			v_distance = v[1]
			if v_id in closed_list: # if already visited, skip
				continue

			G_cost = G[current_vertex] + v_distance

			if G_cost < G[v_id]: # if better cost is found, update
				parent[v_id] = current_vertex
				G[v_id] = G_cost
				H_cost = calculate_euclidean_distance(v_id, destination) # estimated distance from the linked vertex to the end vertex
				F[v_id] = G[v_id] + H_cost
			if v_id not in open_list: 	# if does not exist in the open list, add
				open_list.append(v_id)

	# result
	if F[destination] == INF:
		print("\n\tThe path was not found")
	else:
		print("\tThe number of visited vertices is \033[1m%d\033[0m." % (visited_vertices_count+1))
		print("\tThe cost of the shortest path from %d to %d for informed search is \033[1m%.2f\033[0m." % (source, destination, 
																		F[destination]*distance_coefficient))
	return (path[::-1], visited_vertices) # return a reversed path and visited vertices

# The Main Function
if __name__ == "__main__":
	# read inputs
	read_input(sys.argv[1])

	# call uninformed search
	start_time = time.time()
	(path, visited_vertices) = uninformed_search()
	uninformed_total_time = time.time() - start_time
	print("\t\033[1m%.10f\033[0m seconds took for the uninformed search to find the solution." % (uninformed_total_time))
	print_shortest_path(path)
	show_graph(visited_vertices, 0)

	# call informed search
	start_time = time.time()
	(path, visited_vertices) = informed_search()
	informed_total_time = time.time() - start_time
	print("\t\033[1m%.10f\033[0m seconds took for the informed search to find the solution." % (informed_total_time))
	print_shortest_path(path)
	show_graph(visited_vertices, 1)
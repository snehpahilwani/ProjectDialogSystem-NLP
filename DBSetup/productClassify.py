import sys
import random
from sets import Set

brand = []

def writeAttributeToFile(file, data):
	s = open(file, "w+")
	s.write("Id,Name\n")
	for idx, val in enumerate(data):
		s.write(str(idx+1) + ","+val+"\n")

# Stuff to randomize the data
sizes = ["S", "M", "L", "XL", "XXL"]
writeAttributeToFile("size", sizes)

color = ['blue', 'black', 'white', 'red', 'navy', 'brown', 'grey',
 'green', 'moss', 'beige', 'wheat', 'pink', 'carbonite', 'burgundy']
writeAttributeToFile("color", color)

category = ["pant", "jean", "short", "shirt", "jacket", "skirt", "dress", "legging"]
writeAttributeToFile("category", category)
categoryMap = {
	"shirt": "shirt",
	"tee": "shirt",
	"tunic": "shirt",
	"pant": "pant",
	"trouser": "pant",
	"jean": "jean",
	"skinny": "jean",
	"jeggin": "jean",
	"short": "short",
	"parka": "jacket",
	"jacket": "jacket",
	"sweatshirt": "jacket",
	"hood": "jacket",
	"pullover": "jacket",
	"sweater": "jacket",
	"top": "shirt",
	"skirt": "skirt",
	"dress": "dress",
	"legging": "legging"}

# Output file and its headers
outFile = open("product", "w+")
outFile.write("Id,ItemId,Title,Gender,CategoryId,Price,ColorId,SizeId,BrandId,Department,ImgUrl,Rank\n")

def getSize(input):
	for col in sizes:
		if col in str(input).lower():
			return sizes.index(col) + 1
	return random.randrange(1, 6)

def getColor(input):
	for col in color:
		if col in str(input).lower():
			return color.index(col) + 1
	return random.randrange(1, 4)

def getPrice(input):
	if input == "None":
		return random.randrange(10, 51)
	return input

def AddClassification(filename, gender):
	data = open(filename, "r")
	i = 1
	for line in data:
		row = line.strip().split(',')
		if row[5] not in brand:
			brand.append(row[5])
		row[5] = brand.index(row[5]) + 1

		row[2] = getPrice(row[2])
		row[3] = getColor(row[3])
		row[4] = getSize(row[4])
		row.insert(2, gender)
		val = "shirt"
		for id,value in categoryMap.iteritems():
			if id in row[1].lower():
				val = value

		row.insert(3, category.index(val) + 1)
		row.insert(0, i)
		i += 1
		outFile.write((",".join(str(x) for x in row)) + "\n")


AddClassification(str(sys.argv[1]), str(sys.argv[2])) # Male Products
AddClassification(str(sys.argv[3]), str(sys.argv[4])) # Female Products

writeAttributeToFile("brand", brand)

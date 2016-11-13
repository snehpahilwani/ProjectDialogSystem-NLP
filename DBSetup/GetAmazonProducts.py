import re
from sets import Set
from amazon.api import AmazonAPI
import time

def getProductInfo(pidList, counter):
	amazon = AmazonAPI("", "", "")

	for itemId in pidList: 
		try:
			product = amazon.lookup(ItemId=itemId)
			info = str(itemId)
			info += "," + str(product.title)
			info += "," + str(product.list_price[0])
			info += "," + str(product.color)
			info += "," + str(product.get_attribute('Size'))
			info += "," + str(product.brand)
			info += "," + str(product.get_attribute('ProductGroup'))
			info += "," + str(product.small_image_url)
			info += "," + str(product.sales_rank)
			if (product.get_attribute('ProductGroup') == 'Apparel'):
				w.write(info+"\n")

		except Exception as e:
			print e

		print counter
		counter += 1
		time.sleep(2)

asinRegex = re.compile(r'/([A-Z0-9]{10})')

asinMap = Set()
f = open('pageSource.txt', 'r')
w = open("pageOutWomen.txt", "a+")
for line in f:
	for c in re.findall(asinRegex, line):
		asinMap.add(c)

asinMap = list(asinMap)

print len(asinMap)
getProductInfo(asinMap, 1)



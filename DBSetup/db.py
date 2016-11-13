import sys
import sqlite3 as sql
import pandas as pd

conn = sql.connect('Dialog.db')
df = pd.read_csv("product")
df[['Id','CategoryId','ColorId','SizeId','BrandId']].apply(pd.to_numeric)
df.to_sql("product", conn, if_exists='append', index=False)

df = pd.read_csv("brand")
pd.to_numeric(df.Id)
df.to_sql("brand", conn, if_exists='append', index=False)

df = pd.read_csv("category")
pd.to_numeric(df.Id)
df.to_sql("category", conn, if_exists='append', index=False)

df = pd.read_csv("color")
pd.to_numeric(df.Id)
df.to_sql("color", conn, if_exists='append', index=False)

df = pd.read_csv("size")
pd.to_numeric(df.Id)
df.to_sql("size", conn, if_exists='append', index=False)

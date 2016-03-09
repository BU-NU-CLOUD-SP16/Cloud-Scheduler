import novaclient.client as nvclient
import getpass
import argparse

OS_AUTH_URL = "https://keystone.kaizen.massopencloud.org:5000/v2.0"
OS_USERNAME = "bollapragada.s@husky.neu.edu"
OS_PASSWORD = 'Soumya123'
OS_TENANT_NAME = "Cloud Scheduler"
OS_REGION_NAME = "MOC_Kaizen"

parser = argparse.ArgumentParser()
parser.add_argument('--name',type=str)
parser.add_argument('--image',type=str)
parser.add_argument('--flavor',type=str)
parser.add_argument('--key-name',type=str)

args = parser.parse_args()

name = args.name
image = args.image
flavor = args.flavor
key_name = args.key_name


nova = nvclient.Client("2",auth_url=OS_AUTH_URL,
                       username=OS_USERNAME,
                       api_key=OS_PASSWORD,
                       project_id=OS_TENANT_NAME,
                       region_name=OS_REGION_NAME)

nova.servers.create(name=name,image=image,flavor=flavor,key_name=key_name)
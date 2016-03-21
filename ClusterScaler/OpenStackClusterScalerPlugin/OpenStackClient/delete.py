import novaclient.client as nvclient
import getpass
import argparse


OS_AUTH_URL = "https://keystone.kaizen.massopencloud.org:5000/v2.0"
OS_USERNAME = "bollapragada.s@husky.neu.edu"
OS_TENANT_NAME = "Cloud Scheduler"
OS_REGION_NAME = "MOC_Kaizen"

parser = argparse.ArgumentParser()
parser.add_argument('--id',type=str)
parser.add_argument('--password',type=str)

args = parser.parse_args()

node_id = args.id
OS_PASSWORD = args.password

nova = nvclient.Client("2",auth_url=OS_AUTH_URL,
                       username=OS_USERNAME,
                       api_key=OS_PASSWORD,
                       project_id=OS_TENANT_NAME,
                       region_name=OS_REGION_NAME)


nova.servers.delete(node_id)
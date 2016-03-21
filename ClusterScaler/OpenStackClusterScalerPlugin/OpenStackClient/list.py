import novaclient.client as nvclient
import getpass
import json
import argparse


def convertDict(servers):
    dicts = []

    for server in servers :
        dict = {}
        dict['name'] = server.name
        dict['id'] = server.id
        dict['flavor'] = server.flavor
        dict['status'] = server.status
        try:
            dict['ip'] = server.addresses['Mesos-Cluster'][0]['addr']
        except:
            ""
        dicts.append(dict)

    return dicts

OS_AUTH_URL = "https://keystone.kaizen.massopencloud.org:5000/v2.0"
OS_USERNAME = "bollapragada.s@husky.neu.edu"
OS_TENANT_NAME = "Cloud Scheduler"
OS_REGION_NAME = "MOC_Kaizen"


parser = argparse.ArgumentParser()
parser.add_argument('--name',type=str)
parser.add_argument('--password',type=str)

args = parser.parse_args()

name = args.name
OS_PASSWORD = args.password

nova = nvclient.Client("2",auth_url=OS_AUTH_URL,
                       username=OS_USERNAME,
                       api_key=OS_PASSWORD,
                       project_id=OS_TENANT_NAME,
                       region_name=OS_REGION_NAME)


if name == None:
    name = "slave"

servers = nova.servers.list()
if name != "slave":
    slave_servers = filter(lambda server: name.lower() == server.name.lower(),servers)
else:
    slave_servers = filter(lambda server: name in server.name.lower(),servers)


slave_dicts = convertDict(slave_servers)

json_data = json.dumps(slave_dicts)

print json_data
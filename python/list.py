import novaclient.client as nvclient
import getpass
import json


def convertDict(servers):
    dicts = []

    for server in servers :
        dict = {}
        dict['name'] = server.name
        dict['id'] = server.id
        dict['flavor'] = server.flavor
        dict['ip'] = server.addresses['Mesos-Cluster'][0]['addr']
        dicts.append(dict)

    return dicts

OS_AUTH_URL = "https://keystone.kaizen.massopencloud.org:5000/v2.0"
OS_USERNAME = "bollapragada.s@husky.neu.edu"
OS_PASSWORD = 'Soumya123'
OS_TENANT_NAME = "Cloud Scheduler"
OS_REGION_NAME = "MOC_Kaizen"

nova = nvclient.Client("2",auth_url=OS_AUTH_URL,
                       username=OS_USERNAME,
                       api_key=OS_PASSWORD,
                       project_id=OS_TENANT_NAME,
                       region_name=OS_REGION_NAME)


servers = nova.servers.list()
slave_servers = filter(lambda server: "slave" in server.name.lower(),servers)

slave_dicts = convertDict(slave_servers)

json_data = json.dumps(slave_dicts)

f = open("servers.json","w")

f.write(json_data)
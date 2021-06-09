#import urllib
import requests
#from bs4 import BeautifulSoup
#from io import StringIO,BytesIO
import json

baseurl = "http://10.239.65.153:8111/"
buildtypeid = "Testtc_Build"
buildtypeid_check = "Testtc_CheckBuild"
agentname = "Default Agent"
user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36"
headers1 = {"Accept": "application/json",#text/plain",
        "Accept-Encoding": "gzip, deflate",
        "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8,ru;q=0.7,zh-TW;q=0.6",
        "Cache-Control": "max-age=0",
        "Connection": "keep-alive",
        "Host": "localhost",
        "Upgrade-Insecure-Requests": "1",
        "Cookie": "",
        "Authorization": "Bearer eyJ0eXAiOiAiVENWMiJ9.T2s2eHViMHpaMjRoc3QzcjVYc0licG01d25B.NmJhZTUzNjAtZmIzOC00OTA5LThhM2YtNDg4ZGIxNzc1Yjcw",
        "Content-type": "application/json",
        "User-Agent":user_agent}

tag1 = {
    "name": "test build commit",
    "private": "false",
}

formdata = {
	"branchName": "main",
	"buildType": {
            "id": buildtypeid
	},
	"comment": {
            "text": "test commit"
	},
        "tags": {
            "count": 1,
            "tag": [tag1]
        },
	"agent": {
            "id": 1
	},
        "modificationId":"",
	"properties": {
            #"property": [{
            #	"name": "env.myProperty",
            #   "value": "myValue"
            #   }]
	},
	"personal": "true"
}

class sse_connect():
    def __init__(self):
        self.url_projects = baseurl+"app/rest/projects"
        self.url_last_success = baseurl+"app/rest/builds/?locator=buildType:"+buildtypeid+",status:SUCCESS,count:1"
        self.url_last_fail    = baseurl+"app/rest/builds/?locator=buildType:"+buildtypeid+",status:FAILURE,count:1"
        self.url_last = baseurl+"app/rest/builds/?locator=buildType:"+buildtypeid+",count:1"
        self.url_agentid      = baseurl+"app/rest/agents?locator=name:"+agentname
        #custbuild = "http://localhost/action.html?add2Queue=Test_2_Build&modificationId=15"
        self.url_custbuild = baseurl+"action.html?add2Queue="+buildtypeid+"&modificationId="
        #build_detial = "http://localhost/app/rest/builds/id:203"
        self.url_build_detial_base = baseurl+"app/rest/builds/id:"
        self.url_agents = baseurl+"app/rest/agents?locator=connected:true,authorized:true"
        self.url_add_build = baseurl+"app/rest/buildQueue"
        self.url_change = baseurl+"app/rest/changes/id:"

    def create_session(self, url):
        self.url = url
        session = requests.Session()
        return session

    def rest_get(self, turl):
        res = session.get(turl, headers=headers1)
        res = res.content.decode('UTF-8')
        res = json.loads(res)
        return res

    def get_build_id(self, turl):
        res = self.rest_get(turl)
        #print(res)
        return str(res['build'][0]['id'])

    def get_modify_id(self, turl):
        res = self.rest_get(turl)
        #print("build number:", res['number'])
        #print("change number:", res['lastChanges']['change'][0]['id'])
        #print("commit id:", res['lastChanges']['change'][0]['version'])
        #print(res)
        return res['lastChanges']['change'][0]['id']
    def get_change_commit(self, turl):
        res = self.rest_get(turl)
        return res['version']

    def rebuild_each(self, session):
        #res = session.get(agentid, headers=headers1)
        build_detial_url = self.url_build_detial_base + self.get_build_id(self.url_last)
        last_detial = self.rest_get(build_detial_url)
        print("last build status:", last_detial['status'])

        if last_detial['status'] == 'FAILURE':
            print("try to rebuild eacho commit")
            build_detial_url = self.url_build_detial_base + self.get_build_id(self.url_last_success)
            last_success_modifyid = self.get_modify_id(build_detial_url)

            build_detial_url = self.url_build_detial_base + self.get_build_id(self.url_last_fail)
            last_fail_modifyid = self.get_modify_id(build_detial_url)

            agent_id = self.get_agents(session)
            for i in range(last_success_modifyid, last_fail_modifyid+1):
                #newbuild = self.url_custbuild + str(i)
                #print(newbuild)
                #session.post(newbuild, headers=headers1)
                url_newchange = self.url_change + str(i)
                change_version = self.get_change_commit(url_newchange)
                formdata['modificationId'] = str(i)
                formdata['comment']['text'] = "commit"+ str(i) +": "+change_version
                formdata['agent']['id'] = agent_id[i%len(agent_id)]
                res = self.custom_build(session)
                #print(res.content)

    def custom_build(self, session):
        res = session.post(self.url_add_build, data=json.dumps(formdata), headers=headers1)
        #res = session.get(self.url_agentid, headers=headers1)
        #print(res.content)
        return res

    def get_agents(self, session):
        agent_id = []
        res = self.rest_get(self.url_agents)
        n = res["count"]
        for i in range(n):
            agent_id.append(res['agent'][i]['id'])
        return agent_id

    def close_session(self, session):
        session.close()

if __name__ == "__main__":
    conn = sse_connect()
    session = conn.create_session(baseurl)
    conn.rebuild_each(session)
    conn.close_session(session)

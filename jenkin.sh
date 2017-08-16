mvn package -Dmaven.test.skip=true
mv gateway-web/target/*.war gateway-web-1.8.0.war
REGISTRY_HOST=registry.paas:443
USER_NAME=admin
CI_BUILD_REF_NAME=master
IMAGE=gateway-web-1.8.0
buildtime=$(date +%Y%m%d%H%M%S)
 docker login -u $USER_NAME -p 111111 -e kevin@qq.com $REGISTRY_HOST
 docker build -t registry.paas:443/${USER_NAME}/${IMAGE}:${buildtime} -f Dockerfile .
 docker login -u $USER_NAME -p 111111 -e kevin@qq.com $REGISTRY_HOST
 docker push registry.paas:443/${USER_NAME}/${IMAGE}:${buildtime}
 result=`curl -X POST \
 -d "registry=${REGISTRY_HOST}&userName=$USER_NAME&image=$IMAGE&tag=${buildtime}&description_resource=try_try_try&&description=v1.0&deploy_timeout=180000&start_timeout=120000&stop_timeout=120000&destroy_timeout=120000" \
http://172.16.49.81:5081/svn/ws/registry/registResourceFromCI`
echo $result
if [ "$result" == '{"result":true }' ] ; then
	echo success
else
	exit 1
fi
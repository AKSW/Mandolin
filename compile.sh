echo "Downloading data..."
wget -q https://s3-eu-west-1.amazonaws.com/anonymous-folder/data.zip
unzip data.zip && rm -rf data.zip
echo "Compiling..."
export MAVEN_OPTS=-Xss4m
mvn -q clean compile assembly:single
echo "Done."

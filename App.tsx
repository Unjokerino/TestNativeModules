import React, {useState} from 'react';
import {
  SafeAreaView,
  useColorScheme,
  Button,
  ScrollView,
  Text,
} from 'react-native';

import {Colors} from 'react-native/Libraries/NewAppScreen';
import {RequestParams, Response} from './types';
import RequestModule from './modules';

function App(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const [response, setResponse] = useState<Response>();

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const onMakeRequest = async (url: string, params: RequestParams) => {
    const result = await RequestModule.makeRequest(url, params);
    setResponse(result);
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <Button
        title="Make GET Request"
        onPress={() =>
          onMakeRequest('https://catfact.ninja/fact', {
            type: 'GET',
            headers: {
              'Content-Type': 'application/json',
            },
          })
        }
      />
      <Button
        title="Make POST Request"
        onPress={() =>
          onMakeRequest('https://httpbin.org/post', {
            type: 'POST',

            headers: {},
          })
        }
      />
      <Button
        title="Make Failed Request"
        onPress={() =>
          onMakeRequest('https://httpbin.org/get', {
            type: 'POST',
            headers: {},
            body: {},
          })
        }
      />
      <ScrollView>
        <Text>Result: {response?.type}</Text>
        <Text>Status Code: {response?.statusCode}</Text>
        <Text>
          {response?.type === 'error' ? response.error : response?.data}
        </Text>
      </ScrollView>
    </SafeAreaView>
  );
}

export default App;

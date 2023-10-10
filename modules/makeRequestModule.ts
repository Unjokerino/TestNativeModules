import {NativeModules} from 'react-native';
import {RequestParams, Response} from '../types';

const {RequestModule} = NativeModules;

interface RequestModuleIntreface {
  makeRequest(url: string, params: RequestParams): Promise<Response>;
}

export default RequestModule as RequestModuleIntreface;

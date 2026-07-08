import {
  getDeviceInfo,
  getFingerPrint,
  getIrisCapture,
  getFaceCapture,
} from '../index';
import Rdservice from '../NativeRdservice';

jest.mock('../NativeRdservice', () => ({
  __esModule: true,
  default: {
    getDeviceInfo: jest.fn(() =>
      Promise.resolve({ status: 'SUCCESS', message: '<RDService />' })
    ),
    getFingerPrint: jest.fn(() =>
      Promise.resolve({ status: 'SUCCESS', message: '<PidData />' })
    ),
    getIrisCapture: jest.fn(() =>
      Promise.resolve({ status: 'SUCCESS', message: '<PidData />' })
    ),
    getFaceCapture: jest.fn(() =>
      Promise.resolve({ status: 'SUCCESS', message: '<PidData />' })
    ),
  },
}));

const mocked = Rdservice as jest.Mocked<typeof Rdservice>;

beforeEach(() => {
  jest.clearAllMocks();
});

it('getDeviceInfo forwards the package name', async () => {
  const result = await getDeviceInfo('com.mantra.rdservice');
  expect(mocked.getDeviceInfo).toHaveBeenCalledWith('com.mantra.rdservice');
  expect(result.status).toBe('SUCCESS');
});

it('getFingerPrint defaults pidOption to an empty string', async () => {
  await getFingerPrint('com.mantra.rdservice');
  expect(mocked.getFingerPrint).toHaveBeenCalledWith(
    'com.mantra.rdservice',
    ''
  );
});

it('getFingerPrint forwards custom pidOption', async () => {
  const pidOptions = '<PidOptions ver="1.0" />';
  await getFingerPrint('com.mantra.rdservice', pidOptions);
  expect(mocked.getFingerPrint).toHaveBeenCalledWith(
    'com.mantra.rdservice',
    pidOptions
  );
});

it('getIrisCapture defaults pidOption to an empty string', async () => {
  await getIrisCapture('com.mantra.mis100v2.rdservice');
  expect(mocked.getIrisCapture).toHaveBeenCalledWith(
    'com.mantra.mis100v2.rdservice',
    ''
  );
});

it('getFaceCapture defaults pidOption to an empty string', async () => {
  await getFaceCapture('in.gov.uidai.facerd');
  expect(mocked.getFaceCapture).toHaveBeenCalledWith('in.gov.uidai.facerd', '');
});

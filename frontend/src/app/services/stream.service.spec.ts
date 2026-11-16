import { TestBed } from '@angular/core/testing';
import { StreamService } from './stream.service';

describe('StreamService', () => {
  let service: StreamService;
  let originalEventSource: any;

  // Simple mock for EventSource used by the service
  class MockEventSource {
    url: string;
    onmessage: ((ev: MessageEvent) => any) | null = null;
    onerror: ((ev: Event) => any) | null = null;
    readyState = 0;
    constructor(url: string) {
      this.url = url;
    }
    close() {}
    // Helper to trigger a message event
    emitMessage(data: any) {
      if (this.onmessage) {
        this.onmessage(new MessageEvent('message', { data: JSON.stringify(data) }));
      }
    }
    // Helper to trigger an error event
    emitError() {
      if (this.onerror) {
        this.onerror(new Event('error'));
      }
    }
  }

  beforeEach(() => {
    // Replace the global EventSource with our mock
    originalEventSource = (window as any).EventSource;
    (window as any).EventSource = MockEventSource;

    TestBed.configureTestingModule({});
    service = TestBed.inject(StreamService);
  });

  afterEach(() => {
    // Restore original EventSource after each test
    (window as any).EventSource = originalEventSource;
  });

  it('should emit streamed data as observable values', (done) => {
    const expected = { content: 'hello' };
    service.getStream('test-id').subscribe({
      next: (value) => {
        expect(value).toEqual(expected);
        done();
      },
      error: () => fail('should not error')
    });

    // Grab the mock instance that the service created internally
    const mockEs = (service as any).eventSource as MockEventSource;
    mockEs.emitMessage(expected);
  });

  it('should forward errors as observable errors', (done) => {
    service.getStream('test-id').subscribe({
      next: () => fail('should not emit data'),
      error: (err) => {
        expect(err).toBeTruthy();
        done();
      }
    });

    const mockEs = (service as any).eventSource as MockEventSource;
    mockEs.emitError();
  });
});
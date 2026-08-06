import { trigger, state, style, animate, transition } from '@angular/animations';

export const sidebarAnimations = [
  trigger('openClose', [
    state('open', style({ width: '200px' })),
    state('closed', style({ width: '0', visibility:"hidden" })),
    transition('open => closed', [animate('0.2s')]),
    transition('closed => open', [animate('0.2s')]),
  ]),

  trigger('openClosext', [
    state('open', style({ visibility: 'visible' })),
    state('closed', style({ visibility:"hidden" })),
    transition('open => closed', [animate('0.01s')]),
    transition('closed => open', [animate('0.2s')]),
  ]),
];

// export const sidebardAnimationsext = [
//   trigger('openCloseext', [
//     state('open', style({ width: '200px' })),
//     state('closed', style({ width: '0', overflow:"hidden" })),
//     transition('open => closed', [animate('0.2s')]),
//     transition('closed => open', [animate('0.2s')]),
//   ]),
// ];


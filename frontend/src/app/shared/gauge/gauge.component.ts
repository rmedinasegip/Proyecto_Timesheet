import { Component, Input } from '@angular/core';

/**
 * The app's signature data mark: every raw percentage (avance, efectividad,
 * variación) renders as a small measured tick on a scale instead of a bare
 * number — this app's whole job is tracking things against a plan, so the
 * one recurring visual should say that on sight. Scale is fixed at
 * [-50, 150] so negative variance and >100% overrun both still place
 * sensibly instead of pinning to an edge.
 */
@Component({
  selector: 'app-gauge',
  templateUrl: './gauge.component.html',
  styleUrls: ['./gauge.component.css']
})
export class GaugeComponent {
  @Input() value: number | null | undefined;
  @Input() min = -50;
  @Input() max = 150;

  get position(): number {
    if (this.value == null) return 0;
    const clamped = Math.min(this.max, Math.max(this.min, this.value));
    return ((clamped - this.min) / (this.max - this.min)) * 100;
  }

  get zeroPosition(): number {
    const clamped = Math.min(this.max, Math.max(this.min, 0));
    return ((clamped - this.min) / (this.max - this.min)) * 100;
  }

  get isNegative(): boolean {
    return (this.value ?? 0) < 0;
  }
}

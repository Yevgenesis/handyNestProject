import { describe, expect, it } from "vitest";

import { workspaceLabelKey } from "@/widgets/site-header/site-header";

describe("workspaceLabelKey", () => {
  it("selects navigation label by the strongest current role", () => {
    expect(workspaceLabelKey(["ROLE_USER"])).toBe("customerDashboard");
    expect(workspaceLabelKey(["ROLE_PERFORMER"])).toBe("performerDashboard");
    expect(workspaceLabelKey(["ROLE_USER", "ROLE_ADMIN"])).toBe(
      "adminDashboard",
    );
  });
});

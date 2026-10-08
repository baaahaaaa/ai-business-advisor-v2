import { useState } from "react";

import "./App.css";
import "./auth/auth-ui.css";

import { useAuth } from "./auth/useAuth";

import LoginPage from "./pages/LoginPage";
import RiskAssessmentPage from "./pages/RiskAssessmentPage";
import FraudAssessmentPage from "./pages/FraudAssessmentPage";
import AssessmentHistoryPage from "./pages/AssessmentHistoryPage";

type Screen = "risk" | "fraud" | "history";

function App() {
  const { user, isAuthenticated, signOut } = useAuth();

  const [screen, setScreen] = useState<Screen>("risk");

  if (!isAuthenticated || !user) {
    return <LoginPage />;
  }

  function handleLogout() {
    signOut();
    setScreen("risk");
  }

  return (
    <>
      <div className="app-navigation">
        <div className="navigation-brand">
          <strong>AI Business Advisor</strong>
          <span>Insurance Decision Support</span>
        </div>

        <nav
          className="navigation-tabs"
          aria-label="Assessment navigation"
        >
          <button
            type="button"
            className={
              screen === "risk"
                ? "navigation-tab active"
                : "navigation-tab"
            }
            onClick={() => setScreen("risk")}
          >
            Risk Assessment
          </button>

          <button
            type="button"
            className={
              screen === "fraud"
                ? "navigation-tab active"
                : "navigation-tab"
            }
            onClick={() => setScreen("fraud")}
          >
            Fraud Assessment
          </button>
          <button
            type="button"
            className={
              screen === "history"
                ? "navigation-tab active"
                : "navigation-tab"
            }
            onClick={() => setScreen("history")}
          >
            Assessment History
          </button>
        </nav>

        <div className="navigation-account">
          <div className="navigation-user">
            <strong>
              {user.firstName} {user.lastName}
            </strong>

            <small>{user.role}</small>
          </div>

          <button
            type="button"
            className="logout-button"
            onClick={handleLogout}
          >
            Sign out
          </button>
        </div>
      </div>

      {screen === "risk" ? (
        <RiskAssessmentPage />
      ) : screen === "fraud" ? (
        <FraudAssessmentPage />
      ) : (
        <AssessmentHistoryPage />
      )}
    </>
  );
}

export default App;

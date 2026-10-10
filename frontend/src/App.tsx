import { useState } from "react";

import "./App.css";
import "./auth/auth-ui.css";

import { useAuth } from "./auth/useAuth";

import LoginPage from "./pages/LoginPage";
import RiskAssessmentPage from "./pages/RiskAssessmentPage";
import FraudAssessmentPage from "./pages/FraudAssessmentPage";
import AssessmentHistoryPage from "./pages/AssessmentHistoryPage";
import AdminDashboardPage from "./pages/AdminDashboardPage";
import AdminUsersPage from "./pages/AdminUsersPage";
import AdminAssessmentsPage from "./pages/AdminAssessmentsPage";

type Screen = "risk" | "fraud" | "history" | "admin" | "users" | "admin-assessments";

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
          {user.role === "ADMIN" && (
            <>
              <button
                type="button"
                className={
                  screen === "admin"
                    ? "navigation-tab active"
                    : "navigation-tab"
                }
                onClick={() => setScreen("admin")}
              >
                Admin Dashboard
              </button>

              <button
                type="button"
                className={
                  screen === "users"
                    ? "navigation-tab active"
                    : "navigation-tab"
                }
                onClick={() => setScreen("users")}
              >
                Manage Users
              </button>

              <button
                type="button"
                className={
                  screen === "admin-assessments"
                    ? "navigation-tab active"
                    : "navigation-tab"
                }
                onClick={() => setScreen("admin-assessments")}
              >
                All Assessments
              </button>
            </>
          )}
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
      ) : screen === "history" ? (
        <AssessmentHistoryPage />
      ) : screen === "admin" && user.role === "ADMIN" ? (
        <AdminDashboardPage />
      ) : screen === "users" && user.role === "ADMIN" ? (
        <AdminUsersPage />
      ) : screen === "admin-assessments" && user.role === "ADMIN" ? (
        <AdminAssessmentsPage />
      ) : (
        <RiskAssessmentPage />
      )}
    </>
  );
}

export default App;

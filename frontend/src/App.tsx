import { useState } from "react";

import "./App.css";

import RiskAssessmentPage
  from "./pages/RiskAssessmentPage";

import FraudAssessmentPage
  from "./pages/FraudAssessmentPage";


type Screen =
  | "risk"
  | "fraud";


function App() {

  const [screen, setScreen] =
    useState<Screen>("risk");


  return (
    <>

      <div className="app-navigation">

        <div className="navigation-brand">
          <strong>
            AI Business Advisor
          </strong>

          <span>
            Insurance Decision Support
          </span>
        </div>


        <nav className="navigation-tabs">

          <button
            type="button"
            className={
              screen === "risk"
                ? "navigation-tab active"
                : "navigation-tab"
            }
            onClick={() =>
              setScreen("risk")
            }
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
            onClick={() =>
              setScreen("fraud")
            }
          >
            Fraud Assessment
          </button>

        </nav>

      </div>


      {
        screen === "risk"
          ? <RiskAssessmentPage />
          : <FraudAssessmentPage />
      }

    </>
  );
}


export default App;

import React, { useState } from 'react';

const ProfileCreator = () => {
    const [currentSection, setCurrentSection] = useState(0);
    const [responses, setResponses] = useState({
        username: '',
        lookingFor: [],
        dealBreakers: [],
        interests: [],
        ageRange: '',
        locationPreference: ''
    });
    //create questioningComplete variables -- make it false initially
    const [questioningComplete, setQuestioningComplete] = useState(false);
    const [currentInput, setCurrentInput] = useState('');
    //const history = useHistory(); //history for navigation

    const handleNext = () => {
        switch(currentSection) {
            case 0: // Username
                if (currentInput) {
                    setResponses(prev => ({ ...prev, username: currentInput }));
                    setCurrentSection(1);
                    setCurrentInput('');
                }
                break;
            case 1: // Looking for
            case 2: // Deal breakers
            case 3: // Interests
                const key = ['lookingFor', 'dealBreakers', 'interests'][currentSection - 1];
                if(currentSection == 3)
                    {
                        setQuestioningComplete(true);
                    }
                else ((responses[key]).length > 0) 
                {
                    setCurrentSection(prev => prev + 1);
                    setCurrentInput('');
                }
                break;
            // ... handle other sections
            default:
                break;
        }
    };

    const handlePrevious = () => {
        if(currentSection > 0) {
            setCurrentSection(prev => prev - 1);

            if(currentSection == 4){
                setQuestioningComplete(false);
            }
        }
    };

    const handleContinue = () => {
        setQuestioningComplete(true);
       // history.push('/profile-created');
    }


    const addItem = () => {
        if (!currentInput.trim()) return;

        const key = ['lookingFor', 'dealBreakers', 'interests'][currentSection - 1];
        const cleanedInput = currentInput.trim().replace(/^•\s*/, '');

        setResponses(prev => ({
            ...prev,
            [key]: [...(prev[key]), cleanedInput]
        }));
        setCurrentInput('• ');
    };

    // const debugPrint = () => {
    //     console.log(responses)
    // }

    return (
        <div className="container">
            <div className="profile-card">
            <h1 className="header">HeartHack Profile</h1>
            
            <div className="question-frame">
                <label className="question-label">
                    {/* Question text based on currentSection */
                     ["What's your username?",
                        "What are you looking for in a match?",
                        "What are your deal breakers?",
                        "What are your interests/hobbies?"]
                     [currentSection]}
                </label>

                {/* conditionally render profile summary -- no textbox for this portion */}
               {questioningComplete ? (
                <div className="profile-summary"> 
                <h2>Profile saved successfully!🥳</h2>
                <p>Username: {responses.username}</p>
                <p>Looking For: {responses.lookingFor?.join(", ")}</p>
                <p>Deal Breakers: {responses.dealBreakers?.join(", ")}</p>
                <p>Interests: {responses.interests?.join(", ")}</p>
                </div>
               ) : (
                <>
                <textarea 
                    className="text-input"
                    value={currentInput}
                    onChange={(e) => setCurrentInput(e.target.value)}
                    onKeyPress={(e) => {
                        if (e.key === 'Enter') {
                            e.preventDefault();
                            if ([1,2,3].includes(currentSection)) {
                                addItem();
                            } else {
                                handleNext();
                            }
                        }
                    }}
                />
                

                <ul className="items-list">
                    {[1,2,3].includes(currentSection) && 
                        (responses[['lookingFor', 'dealBreakers', 'interests'][currentSection - 1]])
                        .map((item, index) => (
                            <li key={index}>{item}</li>
                        ))
                    }
                </ul>
                </>
            )}       
                <div className="button-frame">
                    {/* render "next" button when questioning is not complete */}
                    {[1,2,3].includes(currentSection) && (
                        <button className="button" onClick={addItem}>
                            Add Item
                        </button>
                    )}
                   <button 
                        className="button" 
                        onClick={currentSection == 4 ? handleContinue: handleNext}
                    >
                        {currentSection === 4 ? 'Continue' : 'Next'}
                    </button>
                    {currentSection > 0 && (
                    <button className="button" onClick={handlePrevious}> 
                    Back 
                    </button>
                    )}
                </div>
            </div>
           </div>
        </div>
    );
};

export default ProfileCreator; 
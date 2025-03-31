import pandas as pd
import numpy as np
from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler
from sklearn.impute import SimpleImputer
def getScore(data):
    # Load the dataset

    if not data or any(v is None or len(v) == 0 for v in data.values()):
        raise ValueError("Invalid input: Received empty or None lists.")


    df = pd.DataFrame.from_dict(data, orient="index").transpose()

    df_numeric = df.select_dtypes(include=['number'])

    imputer = SimpleImputer(strategy="mean")
    df_imputed = imputer.fit_transform(df_numeric)

    scaler = StandardScaler()
    df_normalized = scaler.fit_transform(df_imputed)

    pca = PCA(n_components=4)
    principal_components = pca.fit_transform(df_normalized)

    gait_scores = np.sum(principal_components, axis=1)
    gait_index = (gait_scores - np.min(gait_scores)) / (np.max(gait_scores) - np.min(gait_scores))

    return np.mean(gait_index)  # Return Mean Gait Index